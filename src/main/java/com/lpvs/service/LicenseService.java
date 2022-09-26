/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.config.WebhookConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class LicenseService {

    private final static String LICENSE_FILE_PATH_PROP_NAME = "license_filepath";
    private final static String LICENSE_CONFLICT_SOURCE_PROP_NAME = "license_conflict";

    private final static String LICENSE_FILE_PATH_ENV_VAR_NAME = "LPVS_LICENSE_FILEPATH";
    private final static String LICENSE_CONFLICT_SOURCE_ENV_VAR_NAME = "LPVS_LICENSE_CONFLICT";

    private final static String LICENSE_FILE_PATH_DEFAULT = "classes/licenses.json";
    private final static String LICENSE_CONFLICT_SOURCE_DEFAULT = "json";

    public String licenseFilePath;
    public String licenseConflictsSource;

    private static final Logger LOG = LoggerFactory.getLogger(LicenseService.class);

    private List<LPVSLicense> licenses;

    private List<Conflict<String, String>> licenseConflicts;
    
    @Autowired
    public LicenseService(@Value("${" + LICENSE_FILE_PATH_PROP_NAME + ":" + LICENSE_FILE_PATH_DEFAULT + "}") String licenseFilePath,
                          @Value("${" + LICENSE_CONFLICT_SOURCE_PROP_NAME + ":" + LICENSE_CONFLICT_SOURCE_DEFAULT + "}") String licenseConflictsSource) {
        this.licenseFilePath = licenseFilePath;
        this.licenseConflictsSource = licenseConflictsSource;
    }

    @Autowired
    ApplicationContext applicationContext;

    @PostConstruct
    private void init() {
        licenseFilePath = (licenseFilePath == null || licenseFilePath.equals(LICENSE_FILE_PATH_DEFAULT))
        && System.getenv(LICENSE_FILE_PATH_ENV_VAR_NAME) != null
                && !System.getenv(LICENSE_FILE_PATH_ENV_VAR_NAME).isEmpty() ?
                System.getenv(LICENSE_FILE_PATH_ENV_VAR_NAME) : licenseFilePath;
        licenseConflictsSource = (licenseConflictsSource == null || licenseConflictsSource.equals(
                LICENSE_CONFLICT_SOURCE_DEFAULT
        ))
        && System.getenv(LICENSE_CONFLICT_SOURCE_ENV_VAR_NAME) != null
                && !System.getenv(LICENSE_CONFLICT_SOURCE_ENV_VAR_NAME).isEmpty() ?
                System.getenv(LICENSE_CONFLICT_SOURCE_ENV_VAR_NAME) : licenseConflictsSource;
        if (licenseFilePath == null || licenseFilePath.isEmpty()) {
            LOG.error(LICENSE_FILE_PATH_ENV_VAR_NAME + "(" + LICENSE_FILE_PATH_PROP_NAME + ") is not set");
            System.exit(SpringApplication.exit(applicationContext, () -> -1));
        }
        if (licenseConflictsSource == null || licenseConflictsSource.isEmpty()) {
            LOG.error(LICENSE_CONFLICT_SOURCE_ENV_VAR_NAME + "(" + LICENSE_CONFLICT_SOURCE_PROP_NAME + ") is not set");
            System.exit(SpringApplication.exit(applicationContext, () -> -1));
        }
        try {
            // 1. Load licenses
            // create Gson instance
            Gson gson = new Gson();
            // create a reader
            Reader reader = Files.newBufferedReader(Paths.get(licenseFilePath));
            // convert JSON array to list of licenses
            licenses = new Gson().fromJson(reader, new TypeToken<List<LPVSLicense>>() {}.getType());
            // print info
            LOG.info("LICENSES: loaded " + licenses.size() + " licenses from JSON file.");
            // close reader
            reader.close();

            // 2. Load license conflicts
            licenseConflicts = new ArrayList<>();

            if (licenseConflictsSource.equalsIgnoreCase("json")) {
                for (LPVSLicense license : licenses) {
                    if (license.getIncompatibleWith() != null && !license.getIncompatibleWith().isEmpty()) {
                        for (String lic : license.getIncompatibleWith()) {
                            Conflict<String, String> conf = new Conflict<>(license.getSpdxId(), lic);
                            if (!licenseConflicts.contains(conf)) {
                                licenseConflicts.add(conf);
                            }
                        }
                    }
                }
                LOG.info("LICENSE CONFLICTS: loaded " + licenseConflicts.size() + " license conflicts.");
            }

        } catch (Exception ex) {
            LOG.error(ex.toString());
            licenses = new ArrayList<>();
            licenseConflicts = new ArrayList<>();
        }
    }

    public LPVSLicense findLicenseBySPDX(String name) {
       for (LPVSLicense license : licenses) {
          if (license.getSpdxId().equalsIgnoreCase(name)) {
              return license;
          }
       }
       return null;
    }

    public LPVSLicense  findLicenseByName(String name) {
        for (LPVSLicense license : licenses) {
            if (license.getLicenseName().equalsIgnoreCase(name)) {
                return license;
            }
        }
        return null;
    }

    public void addLicenseConflict(String license1, String license2) {
        Conflict<String, String> conf = new Conflict<>(license1, license2);
        if (!licenseConflicts.contains(conf)) {
            licenseConflicts.add(conf);
        }
    }


    public LPVSLicense checkLicense(String spdxId) {
        LPVSLicense newLicense = findLicenseBySPDX(spdxId);
        if (newLicense == null && spdxId.contains("+")) {
            newLicense = findLicenseBySPDX(spdxId.replace("+", "") + "-or-later");
        }
        if (newLicense == null && spdxId.contains("+")) {
            newLicense = findLicenseBySPDX(spdxId.replace("+", "") + "-only");
        }
        return newLicense;
    }

    public List<Conflict<String, String>> findConflicts(WebhookConfig webhookConfig, List<LPVSFile> scanResults) {

        if (scanResults.isEmpty() || licenseConflicts.isEmpty()) {
            return null;
        }

        // 0. Extract the set of detected licenses from scan results
        List<String> detectedLicenses = new ArrayList<>();
        for (LPVSFile result : scanResults) {
            for (LPVSLicense license : result.getLicenses()) {
                detectedLicenses.add(license.getSpdxId());
            }
        }
        // leave license SPDX IDs without repetitions
        Set<String> detectedLicensesUnique = new HashSet<>(detectedLicenses);

        // 1. Check conflict between repository license and detected licenses
        List<Conflict<String, String>> foundConflicts = new ArrayList<>();
        String repositoryLicense = webhookConfig.getRepositoryLicense();
        if (repositoryLicense != null) {
            for (String detectedLicenseUnique : detectedLicensesUnique) {
                for (Conflict<String, String> licenseConflict : licenseConflicts) {
                    Conflict<String, String> possibleConflict = new Conflict<>(detectedLicenseUnique, repositoryLicense);
                    if (licenseConflict.equals(possibleConflict)) {
                        foundConflicts.add(possibleConflict);
                    }
                }
            }
        }

        // 2. Check conflict between detected licenses
        for (int i = 0; i < detectedLicensesUnique.size(); i++) {
            for (int j = i + 1; j < detectedLicensesUnique.size(); j++) {
                for (Conflict<String, String> licenseConflict : licenseConflicts) {
                    Conflict<String, String> possibleConflict =
                            new Conflict<>(
                                    (String) detectedLicensesUnique.toArray()[i],
                                    (String) detectedLicensesUnique.toArray()[j]
                            );
                    if (licenseConflict.equals(possibleConflict)) {
                        foundConflicts.add(possibleConflict);
                    }
                }
            }
        }

        return foundConflicts;
    }

    public static class Conflict<License1, License2> {
        public License1 l1;
        public License2 l2;
        Conflict(License1 l1, License2 l2) {
            this.l1 = l1;
            this.l2 = l2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Conflict<?, ?> conflict = (Conflict<?, ?>) o;
            return (l1.equals(conflict.l1) && l2.equals(conflict.l2)) ||
                    (l1.equals(conflict.l2) && l2.equals(conflict.l1));
        }

        @Override
        public int hashCode() {
            return Objects.hash(l1, l2);
        }
    }
}
