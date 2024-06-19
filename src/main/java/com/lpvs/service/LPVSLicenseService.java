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

import com.lpvs.util.LPVSPayloadUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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
     * URL of the source of the new licenses.
     */
    @Value("${license_source:}")
    private String osoriDbUrl;

    /**
     * The object used to make HTTP requests to the OSORI DB.
     */
    private OsoriConnection osoriConnection = new OsoriConnection();

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
     * Load all license conflicts from the database.
     */
    private void loadLicenseConflicts() {
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
                    loadLicenseConflicts();
                    // print info
                    log.info(
                            "LICENSE CONFLICTS: loaded "
                                    + licenseConflicts.size()
                                    + " license conflicts from DB.");
                }

            } catch (Exception ex) {
                log.warn("LICENSES and LICENSE CONFLICTS are not loaded.");
                log.error(ex.getMessage());
                licenses = new ArrayList<>();
                licenseConflicts = new ArrayList<>();
            }
        }
    }

    /**
     * This method reloads licenses and license conflicts from database tables if the
     * licenses list in case of single scan triggered and used in memory database.
     */
    public void reloadFromTables() {
        if (licenses.isEmpty()) {
            licenses = lpvsLicenseRepository.takeAllLicenses();
            log.info("RELOADED " + licenses.size() + " licenses from DB.");

            loadLicenseConflicts();
            log.info("RELOADED " + licenseConflicts.size() + " license conflicts from DB.");
        }
    }

    /**
     * Finds a license by SPDX identifier.
     *
     * @param name SPDX identifier of the license.
     * @return LPVSLicense object if found, otherwise null.
     */
    protected LPVSLicense findLicenseBySPDX(String name) {
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
    protected void addLicenseToList(LPVSLicense license) {
        if (!licenses.contains(license)) {
            licenses.add(license);
        }
    }

    /**
     * Finds a license by its name.
     *
     * @param name The name of the license.
     * @return LPVSLicense object if found, otherwise null.
     */
    protected LPVSLicense findLicenseByName(String name) {
        for (LPVSLicense license : licenses) {
            if (license.getLicenseName().equalsIgnoreCase(name)) {
                return license;
            }
            if (license.getAlternativeNames() != null && !license.getAlternativeNames().isBlank()) {
                String[] names = license.getAlternativeNames().split(",");
                for (String n : names) {
                    if (n.trim().equalsIgnoreCase(name)) {
                        return license;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets a license object from the database using the specified license SPDX ID and license name.
     * @param licenseSpdxId The license SPDX ID to search for
     * @param licenseName The license name to use if the license is not found in the database
     * @return The license object that was found or created
     */
    public LPVSLicense getLicenseBySpdxIdAndName(
            String licenseSpdxId, Optional<String> licenseName) {
        String licName = licenseName.orElse(licenseSpdxId);
        // Check if the license exists in the database
        LPVSLicense lic = findLicense(licenseSpdxId, licName);
        // If not found, check OSORI DB and create a new license
        if (lic == null) {
            lic = findLicenseInOsoriDB(licenseSpdxId);
            // If not found, create new license with default field values
            if (lic == null) {
                lic =
                        new LPVSLicense() {
                            {
                                setSpdxId(licenseSpdxId);
                                setLicenseName(licName);
                                setAlternativeNames(null);
                                setAccess("UNREVIEWED");
                            }
                        };
            }
            // Save new license
            lic = lpvsLicenseRepository.saveAndFlush(lic);
            // Add license to the license list
            addLicenseToList(lic);
        }
        return lic;
    }

    /**
     * Search for a license with the given SPDX identifier in the OSORI database.
     *
     * @param licenseSpdxId The SPDX identifier of the license to search for.
     * @return The LPVSLicense object if the license is found in the OSORI database, otherwise null.
     */
    public LPVSLicense findLicenseInOsoriDB(String licenseSpdxId) {
        // Check if the OSORI database URL is valid
        if (osoriDbUrl == null || osoriDbUrl.trim().isEmpty()) {
            return null;
        }
        // Try to find the license in the OSORI database
        try {
            HttpURLConnection connection =
                    osoriConnection.createConnection(osoriDbUrl, licenseSpdxId);
            connection.setRequestMethod("GET");
            connection.connect();

            // Check if the HTTP response code is 200 (OK)
            if (connection.getResponseCode() != 200) {
                throw new Exception(
                        "HTTP error code ("
                                + connection.getResponseCode()
                                + "): "
                                + connection.getResponseMessage());
            }

            // Convert the response InputStream to a string
            String response =
                    LPVSPayloadUtil.convertInputStreamToString(connection.getInputStream());
            // If the license is found, create a new LPVSLicense object with the field values from
            // the OSORI database
            return LPVSPayloadUtil.convertOsoriDbResponseToLicense(response);
        } catch (Exception e) {
            log.error("Error connecting OSORI DB: " + e.getMessage());
            return null;
        }
    }

    /**
     * Search for a license using the given SPDX identifier and license name. It first tries to find the license by its
     * SPDX identifier, and if that fails, it checks by the license name and alternative names.
     *
     * @param licenseSpdxId The SPDX identifier of the license to search for.
     * @param licenseName   The name or alternative name of the license to search for.
     * @return The LPVSLicense object if the license is found, otherwise null.
     */
    public LPVSLicense findLicense(String licenseSpdxId, String licenseName) {
        // check by license SPDX ID
        LPVSLicense lic = findLicenseBySPDX(licenseSpdxId);
        if (null == lic) {
            // check by license name and alternative names
            lic = findLicenseByName(licenseName);
        }
        return lic;
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

        if (repositoryLicense != null) {
            LPVSLicense repoLicense = lpvsLicenseRepository.searchBySpdxId(repositoryLicense);
            if (repoLicense == null) {
                repoLicense =
                        lpvsLicenseRepository.searchByAlternativeLicenseNames(repositoryLicense);
            }

            if (repoLicense != null) {
                repositoryLicense = repoLicense.getSpdxId();
            }

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
        public Conflict(License1 l1, License2 l2) {
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

    /**
     * The OsoriConnection class provides methods for creating a connection to the OSORI database.
     */
    @NoArgsConstructor
    public static class OsoriConnection {

        /**
         * Creates a connection to the OSORI database using the specified OSORI server and license SPDX identifier.
         *
         * @param osoriDbUrl     The URL of the OSORI server.
         * @param licenseSpdxId  The license SPDX identifier.
         * @return A HttpURLConnection object representing the connection to the OSORI database.
         */
        public HttpURLConnection createConnection(String osoriDbUrl, String licenseSpdxId)
                throws IOException {
            URL url =
                    new URL(
                            osoriDbUrl
                                    + "/api/v1/user/licenses/spdx_identifier?searchWord="
                                    + URLEncoder.encode(licenseSpdxId, "UTF-8"));
            return (HttpURLConnection) url.openConnection();
        }
    }
}
