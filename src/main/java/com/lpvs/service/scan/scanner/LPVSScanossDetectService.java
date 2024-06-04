/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service.scan.scanner;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.repository.LPVSLicenseRepository;
import com.lpvs.service.LPVSLicenseService;
import com.lpvs.service.scan.LPVSScanService;
import com.lpvs.util.LPVSFileUtil;
import com.lpvs.util.LPVSPayloadUtil;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.lpvs.util.LPVSFileUtil.getScanResultsDirectoryPath;
import static com.lpvs.util.LPVSFileUtil.getScanResultsJsonFilePath;

/**
 * Service class responsible for interacting with the Scanoss scanner to scan licenses in files.
 * It handles the initiation of the scan, processing scan results, and checking for license conflicts.
 */
@Service
@Slf4j
public class LPVSScanossDetectService implements LPVSScanService {

    /**
     * The service for managing licenses, providing operations related to licenses.
     */
    @Autowired private LPVSLicenseService licenseService;

    /**
     * The repository for LPVSLicense entities, allowing database interactions for licenses.
     */
    @Autowired private LPVSLicenseRepository lpvsLicenseRepository;

    /**
     * Flag indicating whether the application is in debug mode.
     */
    private Boolean debug;

    /**
     * Constructor for LPVSScanossDetectService.
     *
     * @param debug                 Flag indicating whether the application is in debug mode.
     * @param licenseService        The LPVSLicenseService for license-related operations.
     * @param lpvsLicenseRepository The repository for LPVSLicense entities.
     */
    @Autowired
    public LPVSScanossDetectService(
            @Value("${debug:false}") Boolean debug,
            LPVSLicenseService licenseService,
            LPVSLicenseRepository lpvsLicenseRepository) {
        this.debug = debug;
        this.licenseService = licenseService;
        this.lpvsLicenseRepository = lpvsLicenseRepository;
    }

    /**
     * Initiates the Scanoss scan for the specified LPVSQueue and file path.
     *
     * @param webhookConfig The LPVSQueue representing the GitHub webhook configuration.
     * @param path           The file path to be scanned.
     * @throws Exception If an error occurs during the scanning process.
     */
    public void runScan(LPVSQueue webhookConfig, String path) throws Exception {
        log.debug("Starting Scanoss scanning");
        Process process = null;
        try {
            File resultsDir = new File(getScanResultsDirectoryPath(webhookConfig));
            if (resultsDir.mkdirs()) {
                log.debug(
                        "Scan result directory has been created: " + resultsDir.getAbsolutePath());
            } else if (resultsDir.isDirectory()) {
                log.debug(
                        "Scan result directory already been created: "
                                + resultsDir.getAbsolutePath());
            } else {
                log.error("Directory %s could not be created." + resultsDir.getAbsolutePath());
            }

            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            "scanoss-py",
                            "scan",
                            debug ? "-t" : "-q",
                            "--no-wfp-output",
                            "--all-extensions",
                            "-o",
                            getScanResultsJsonFilePath(webhookConfig),
                            path);

            process = processBuilder.inheritIO().start();

            int status = process.waitFor();

            if (status == 1) {
                log.error("Scanoss scanner terminated with non-zero code. Terminating.");
                BufferedReader output = null;
                try {
                    output =
                            LPVSPayloadUtil.createBufferReader(
                                    LPVSPayloadUtil.createInputStreamReader(
                                            process.getErrorStream()));
                    String line = output.readLine();
                    if (line != null) {
                        log.error(line);
                    }
                    throw new Exception(
                            "Scanoss scanner terminated with non-zero code. Terminating.");
                } finally {
                    if (output != null) output.close();
                }
            }
        } catch (IOException | InterruptedException ex) {
            log.error("Scanoss scanner terminated with non-zero code. Terminating.");
            throw ex;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        log.debug("Scanoss scan done");
    }

    /**
     * Checks the licenses detected by Scanoss and returns a list of LPVSFile entities.
     *
     * @param webhookConfig The LPVSQueue representing the GitHub webhook configuration.
     * @return A list of LPVSFile entities representing detected files and their licenses.
     */
    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    public List<LPVSFile> checkLicenses(LPVSQueue webhookConfig) {
        List<LPVSFile> detectedFiles = new ArrayList<>();
        try {
            Reader reader = getReader(webhookConfig);

            // convert JSON file to map
            Map<String, ArrayList<Object>> map =
                    new Gson()
                            .fromJson(
                                    reader,
                                    new TypeToken<Map<String, ArrayList<Object>>>() {}.getType());
            if (null == map) {
                log.error("Error parsing Json File");
                return detectedFiles;
            }

            // parse map entries
            long ind = 0L;
            for (Map.Entry<String, ArrayList<Object>> entry : map.entrySet()) {
                LPVSFile file = new LPVSFile();
                file.setId(ind++);
                file.setFilePath(entry.getKey().toString());

                String content = buildContent(entry);
                ScanossJsonStructure object = getScanossJsonStructure(content, file);
                Set<LPVSLicense> licenses = buildLicenseSet(object);
                file.setLicenses(new HashSet<>(licenses));
                if (!file.getLicenses().isEmpty()) detectedFiles.add(file);
            }

            // close reader
            reader.close();
        } catch (IOException | IllegalArgumentException ex) {
            log.error("Error while processing Webhook ID = " + webhookConfig.getId());
            log.error(ex.getMessage());
        }
        return detectedFiles;
    }

    /**
     * Builds a set of LPVSLicense objects detected by Scanoss in the given ScanossJsonStructure structure.
     *
     * @param object The ScanossJsonStructure containing information about the licenses detected by Scanoss.
     * @return A set of LPVSLicense objects representing the detected licenses with additional metadata.
     */
    private Set<LPVSLicense> buildLicenseSet(ScanossJsonStructure object) {
        if (object.licenses == null) {
            return new HashSet<>();
        }

        Set<LPVSLicense> licenses = new HashSet<>();
        for (ScanossJsonStructure.ScanossLicense license : object.licenses) {
            // Check detected licenses
            LPVSLicense lic =
                    licenseService.getLicenseBySpdxIdAndName(license.name, Optional.empty());
            lic.setChecklistUrl(license.checklist_url);
            licenses.add(lic);

            // Check for the license conflicts if the property
            // "license_conflict=scanner"
            if (licenseService.licenseConflictsSource.equalsIgnoreCase("scanner")) {
                if (license.incompatible_with != null) {
                    for (String incompatibleLicense : license.incompatible_with) {
                        licenseService.addLicenseConflict(incompatibleLicense, license.name);
                    }
                }
            }
        }
        return licenses;
    }

    /**
     * Parses the content returned by Scanoss and populates the given LPVSFile entity with the relevant information.
     *
     * @param content The string content returned by Scanoss.
     * @param file The LPVSFile entity to be populated with the parsed information.
     * @return The parsed ScanossJsonStructure object containing the extracted information.
     */
    private ScanossJsonStructure getScanossJsonStructure(String content, LPVSFile file) {
        ScanossJsonStructure object = new Gson().fromJson(content, ScanossJsonStructure.class);
        if (object.id != null) file.setSnippetType(object.id);
        if (object.matched != null) file.setSnippetMatch(object.matched);
        if (object.lines != null) file.setMatchedLines(object.lines);
        if (object.file != null) file.setComponentFilePath(object.file);
        if (object.file_url != null) file.setComponentFileUrl(object.file_url);
        if (object.component != null) file.setComponentName(object.component);
        if (object.oss_lines != null) file.setComponentLines(object.oss_lines);
        if (object.url != null) file.setComponentUrl(object.url);
        if (object.version != null) file.setComponentVersion(object.version);
        if (object.vendor != null) file.setComponentVendor(object.vendor);
        return object;
    }

    /**
     * Creates a new BufferedReader object for reading the JSON file containing the scan results.
     *
     * @param webhookConfig The LPVSQueue representing the GitHub webhook configuration.
     * @return A BufferedReader object for reading the JSON file.
     * @throws IOException If an error occurs while creating the BufferedReader object.
     */
    private static Reader getReader(LPVSQueue webhookConfig) throws IOException {
        return Files.newBufferedReader(
                Paths.get(LPVSFileUtil.getScanResultsJsonFilePath(webhookConfig)));
    }

    /**
     * Builds the content string for the given Map.Entry object.
     *
     * @param entry The Map.Entry object containing the information to be included in the content string.
     * @return The constructed content string.
     */
    private static String buildContent(Map.Entry<String, ArrayList<Object>> entry) {
        String content =
                entry.getValue()
                        .toString()
                        .replaceAll("=\\[", "\" : [")
                        .replaceAll("=", "\" : \"")
                        .replaceAll("\\}, \\{", "\"},{")
                        .replaceAll("\\}\\],", "\"}],")
                        .replaceAll("\\{", "{\"")
                        .replaceAll(", ", "\", \"")
                        .replaceAll("\\]\"", "]")
                        .replaceAll("}\",", "\"},")
                        .replaceAll(": \\[", ": [\"")
                        .replaceAll("],", "\"],")
                        .replaceAll("\"\\{\"", "{\"")
                        .replaceAll("\"}\"]", "\"}]")
                        .replaceAll("\\[\"\"\\]", "[]")
                        .replaceAll(
                                "incompatible_with\" : (\".*?\"), \"name",
                                "incompatible_with\" : \\[$1\\], \"name");
        content = content.substring(1, content.length() - 1);
        if (content.endsWith("}")) {
            content = content.substring(0, content.length() - 1) + "\"}";
        }
        return content.replaceAll("}\"}", "\"}}");
    }

    /**
     * Represents the JSON structure of Scanoss scan results.
     */
    @SuppressFBWarnings(
            value = {"UUF_UNUSED_FIELD", "SIC_INNER_SHOULD_BE_STATIC"},
            justification = "Parser class for Json deserialization")
    private class ScanossJsonStructure {
        private String component;
        private String file;
        private String file_hash;
        private String file_url;
        private String id;
        private String latest;
        private ArrayList<ScanossLicense> licenses;
        private String lines;
        private String matched;
        private String oss_lines;
        private ArrayList<String> purl;
        private String release_date;
        private ScanossServer server;
        private String source_hash;
        private String status;
        private String url;
        private String url_hash;
        private String vendor;
        private String version;

        private class ScanossLicense {
            private String checklist_url;
            private String copyleft;
            private ArrayList<String> incompatible_with;
            private String name;
            private String osadl_updated;
            private String patent_hints;
            private String source;
            private String url;
        }

        private class ScanossServer {
            private KbVersion kb_version;
            private String version;

            private class KbVersion {
                private String daily;
                private String monthly;
            }
        }
    }
}
