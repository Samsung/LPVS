/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.LPVSLicenseConflict;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.repository.LPVSLicenseConflictRepository;
import com.lpvs.repository.LPVSLicenseRepository;
import com.lpvs.util.LPVSExitHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.*;

/**
 * Service class for managing licenses and license conflicts.
 *
 * It provides methods to retrieve licenses from a database, check for license conflicts,
 * and handle license-related operations.
 */
@Service
@Slf4j
public class LPVSLicenseService {

    /**
     * Property name for specifying the source of license conflicts.
     */
    private static final String LICENSE_CONFLICT_SOURCE_PROP_NAME = "license_conflict";

    /**
     * Environment variable name for specifying the source of license conflicts.
     */
    private static final String LICENSE_CONFLICT_SOURCE_ENV_VAR_NAME = "LPVS_LICENSE_CONFLICT";

    /**
     * Default source of license conflicts.
     */
    private static final String LICENSE_CONFLICT_SOURCE_DEFAULT = "db";

    /**
     * Source of license conflicts.
     */
    public String licenseConflictsSource;

    /**
     * List of licenses loaded from the database.
     */
    private List<LPVSLicense> licenses = new ArrayList<>();

    /**
     * List of license conflicts loaded from the database.
     */
    private List<Conflict<String, String>> licenseConflicts = new ArrayList<>();

    /**
     * Handler for exiting the application.
     */
    private LPVSExitHandler exitHandler;

    /**
     * Repository for accessing and managing LPVSLicense entities in the database.
     */
    @Autowired private LPVSLicenseRepository lpvsLicenseRepository;

    /**
     * Repository for accessing and managing LPVSLicenseConflict entities in the database.
     */
    @Autowired private LPVSLicenseConflictRepository lpvsLicenseConflictRepository;

    /**
     * Constructs an instance of LPVSLicenseService with the specified license conflicts source and exit handler.
     *
     * @param licenseConflictsSource Source of license conflicts.
     * @param exitHandler            Handler for exiting the application.
     */
    @Autowired
    public LPVSLicenseService(
            @Value(
                            "${"
                                    + LICENSE_CONFLICT_SOURCE_PROP_NAME
                                    + ":"
                                    + LICENSE_CONFLICT_SOURCE_DEFAULT
                                    + "}")
                    String licenseConflictsSource,
            LPVSExitHandler exitHandler) {
        this.licenseConflictsSource = licenseConflictsSource;
        this.exitHandler = exitHandler;
    }

    /**
     * Initializes the LPVSLicenseService by loading licenses and license conflicts from the database.
     */
    @PostConstruct
    private void init() {
        licenseConflictsSource =
                (licenseConflictsSource == null
                                        || licenseConflictsSource.equals(
                                                LICENSE_CONFLICT_SOURCE_DEFAULT))
                                && System.getenv(LICENSE_CONFLICT_SOURCE_ENV_VAR_NAME) != null
                                && !System.getenv(LICENSE_CONFLICT_SOURCE_ENV_VAR_NAME).isEmpty()
                        ? System.getenv(LICENSE_CONFLICT_SOURCE_ENV_VAR_NAME)
                        : licenseConflictsSource;

        if (licenseConflictsSource == null || licenseConflictsSource.isEmpty()) {
            log.error(
                    LICENSE_CONFLICT_SOURCE_ENV_VAR_NAME
                            + "("
                            + LICENSE_CONFLICT_SOURCE_PROP_NAME
                            + ") is not set");
            exitHandler.exit(-1);
        } else {
            try {
                // 1. Load licenses from DB
                licenses = lpvsLicenseRepository.takeAllLicenses();
                // print info
                log.info("LICENSES: loaded " + licenses.size() + " licenses from DB.");

                // 2. Load license conflicts
                licenseConflicts = new ArrayList<>();

                if (licenseConflictsSource.equalsIgnoreCase("db")) {
                    List<LPVSLicenseConflict> conflicts =
                            lpvsLicenseConflictRepository.takeAllLicenseConflicts();
                    for (LPVSLicenseConflict conflict : conflicts) {
                        Conflict<String, String> conf =
                                new Conflict<>(
                                        conflict.getConflictLicense().getSpdxId(),
                                        conflict.getRepositoryLicense().getSpdxId());
                        if (!licenseConflicts.contains(conf)) {
                            licenseConflicts.add(conf);
                        }
                    }
                    // print info
                    log.info(
                            "LICENSE CONFLICTS: loaded "
                                    + licenseConflicts.size()
                                    + " license conflicts from DB.");
                }

            } catch (Exception ex) {
                log.warn("LICENSES and LICENSE CONFLICTS are not loaded.");
                log.error(ex.toString());
                licenses = new ArrayList<>();
                licenseConflicts = new ArrayList<>();
            }
        }
    }

    /**
     * Finds a license by SPDX identifier.
     *
     * @param name SPDX identifier of the license.
     * @return LPVSLicense object if found, otherwise null.
     */
    public LPVSLicense findLicenseBySPDX(String name) {
        for (LPVSLicense license : licenses) {
            if (license.getSpdxId().equalsIgnoreCase(name)) {
                return license;
            }
        }
        return null;
    }

    /**
     * Adds a license to the list of licenses.
     *
     * @param license The license to add.
     */
    public void addLicenseToList(LPVSLicense license) {
        licenses.add(license);
    }

    /**
     * Finds a license by its name.
     *
     * @param name The name of the license.
     * @return LPVSLicense object if found, otherwise null.
     */
    public LPVSLicense findLicenseByName(String name) {
        for (LPVSLicense license : licenses) {
            if (license.getLicenseName().equalsIgnoreCase(name)) {
                return license;
            }
        }
        return null;
    }

    /**
     * Adds a license conflict to the list of conflicts.
     *
     * @param license1 The first license in conflict.
     * @param license2 The second license in conflict.
     */
    public void addLicenseConflict(String license1, String license2) {
        Conflict<String, String> conf = new Conflict<>(license1, license2);
        if (!licenseConflicts.contains(conf)) {
            licenseConflicts.add(conf);
        }
    }

    /**
     * Checks a license by SPDX identifier and returns the corresponding license object.
     *
     * @param spdxId SPDX identifier of the license.
     * @return The corresponding LPVSLicense object.
     */
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

    /**
     * Finds license conflicts based on the provided scan results and repository information.
     *
     * @param webhookConfig Configuration related to the repository and webhook.
     * @param scanResults    List of scanned files.
     * @return List of license conflicts found.
     */
    public List<Conflict<String, String>> findConflicts(
            LPVSQueue webhookConfig, List<LPVSFile> scanResults) {
        List<Conflict<String, String>> foundConflicts = new ArrayList<>();

        if (scanResults.isEmpty() || licenseConflicts.isEmpty()) {
            return foundConflicts;
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
        String repositoryLicense = webhookConfig.getRepositoryLicense();
        // ToDo: add check for license alternative names. Reason: GitHub can use not SPDX ID.
        if (repositoryLicense != null) {
            for (String detectedLicenseUnique : detectedLicensesUnique) {
                for (Conflict<String, String> licenseConflict : licenseConflicts) {
                    Conflict<String, String> possibleConflict =
                            new Conflict<>(detectedLicenseUnique, repositoryLicense);
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
                                    (String) detectedLicensesUnique.toArray()[j]);
                    if (licenseConflict.equals(possibleConflict)) {
                        foundConflicts.add(possibleConflict);
                    }
                }
            }
        }

        return foundConflicts;
    }

    /**
     * Represents a license conflict between two licenses.
     *
     * @param <License1> Type of the first license.
     * @param <License2> Type of the second license.
     */
    public static class Conflict<License1, License2> {

        /**
         * The first license in the conflict.
         */
        public License1 l1;

        /**
         * The second license in the conflict.
         */
        public License2 l2;

        /**
         * Constructs a Conflict object with the specified licenses.
         *
         * @param l1 The first license.
         * @param l2 The second license.
         */
        Conflict(License1 l1, License2 l2) {
            this.l1 = l1;
            this.l2 = l2;
        }

        /**
         * Compares this Conflict object with another object for equality.
         *
         * @param o The object to compare with this Conflict.
         * @return {@code true} if the objects are equal, {@code false} otherwise.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Conflict<?, ?> conflict = (Conflict<?, ?>) o;
            return (l1.equals(conflict.l1) && l2.equals(conflict.l2))
                    || (l1.equals(conflict.l2) && l2.equals(conflict.l1));
        }

        /**
         * Generates a hash code value for this Conflict object.
         * The hash code is computed based on the hash codes of the two licenses.
         *
         * @return A hash code value for this Conflict object.
         */
        @Override
        public int hashCode() {
            return Objects.hash(l1, l2);
        }
    }
}
