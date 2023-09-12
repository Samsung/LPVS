/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service.scanner.scanoss;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.repository.LPVSLicenseRepository;
import com.lpvs.service.LPVSGitHubService;
import com.lpvs.service.LPVSLicenseService;
import com.lpvs.util.LPVSWebhookUtil;

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

@Service
@Slf4j
public class LPVSScanossDetectService {

    @Autowired
    private LPVSLicenseService licenseService;

    @Autowired
    private LPVSGitHubService gitHubService;

    @Autowired
    private LPVSLicenseRepository lpvsLicenseRepository;

    @Value("${debug:false}")
    private Boolean debug;

    public void runScan(LPVSQueue webhookConfig, String path) throws Exception {
        log.debug("Starting Scanoss scanning");

        try {
            ProcessBuilder processBuilder;
            File resultsDir = new File(getScanResultsDirectoryPath(webhookConfig));
            if (resultsDir.mkdirs()) {
                log.debug("Scan result directory has been created: " + resultsDir.getAbsolutePath());
            } else if (resultsDir.isDirectory()) {
                log.debug("Scan result directory already been created: " + resultsDir.getAbsolutePath());
            } else {
                log.error("Directory %s could not be created." + resultsDir.getAbsolutePath());
            }
            processBuilder = new ProcessBuilder(
                "scanoss-py", "scan",
                debug ? "-t" : "-q",
                "--no-wfp-output",
                "-o", getScanResultsJsonFilePath(webhookConfig),
                path
            );
            Process process = processBuilder.inheritIO().start();

            int status = process.waitFor();

            if (status == 1) {
                log.error("Scanoss scanner terminated with none-zero code. Terminating.");
                BufferedReader output = null;
                try {
                    output = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));
                    log.error(output.readLine());
                    throw new Exception("Scanoss scanner terminated with none-zero code. Terminating.");
                }
                finally {
                    if(output != null)
                        output.close();
                }
            }
        } catch (IOException | InterruptedException ex) {
            log.error("Scanoss scanner terminated with none-zero code. Terminating.");
            throw ex;
        }

        log.debug("Scanoss scan done");
    }

    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    public List<LPVSFile> checkLicenses(LPVSQueue webhookConfig) {
        List<LPVSFile> detectedFiles = new ArrayList<>();
        try {
            Gson gson = new Gson();
            Reader reader;
            if (webhookConfig.getHeadCommitSHA() == null || webhookConfig.getHeadCommitSHA().equals("")){
                reader = Files.newBufferedReader(Paths.get(System.getProperty("user.home") + "/" + "Results/" +
                        LPVSWebhookUtil.getRepositoryName(webhookConfig) + "/" + LPVSWebhookUtil.getPullRequestId(webhookConfig) + ".json"));
            } else {
                reader = Files.newBufferedReader(Paths.get(System.getProperty("user.home") + "/" + "Results/" +
                        LPVSWebhookUtil.getRepositoryName(webhookConfig) + "/" + webhookConfig.getHeadCommitSHA() + ".json"));
            }
            // convert JSON file to map
            Map<String, ArrayList<Object>> map = gson.fromJson(reader,
                    new TypeToken<Map<String, ArrayList<Object>>>() {}.getType());

            // parse map entries
            long ind = 0L;
            if (null == map) {
                log.error("Error parsing Json File");
                return detectedFiles;
            }
            for (Map.Entry<String, ArrayList<Object>> entry : map.entrySet()) {
                LPVSFile file = new LPVSFile();
                file.setId(ind++);
                file.setFilePath(entry.getKey().toString());

                String content = entry.getValue().toString()
                        .replaceAll("=\\[", "\" : [")
                        .replaceAll("=", "\" : \"")
                        .replaceAll("\\}, \\{", "\"},{")
                        .replaceAll("\\}\\],", "\"}],")
                        .replaceAll("\\{", "{\"")
                        .replaceAll(", ", "\", \"")
                        .replaceAll("\\]\"","]")
                        .replaceAll("}\",", "\"},")
                        .replaceAll(": \\[", ": [\"")
                        .replaceAll("],", "\"],")
                        .replaceAll("\"\\{\"","{\"")
                        .replaceAll("\"}\"]", "\"}]")
                        .replaceAll("\\[\"\"\\]", "[]")
                        .replaceAll("incompatible_with\" : (\".*?\"), \"name", "incompatible_with\" : \\[$1\\], \"name")
                        ;
                content = content.substring(1, content.length() - 1);
                if(content.endsWith("}"))
                {
                    content = content.substring(0, content.length() - 1) + "\"}";
                }
                content = content.replaceAll("}\"}", "\"}}");
                ScanossJsonStructure object = gson.fromJson(content, ScanossJsonStructure.class);
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

                Set<LPVSLicense> licenses = new HashSet<>();
                if (object.licenses != null) {
                    for (ScanossJsonStructure.ScanossLicense license : object.licenses) {
                        // Check detected licenses
                        LPVSLicense lic = licenseService.findLicenseBySPDX(license.name);
                        if (lic != null) {
                            lic.setChecklistUrl(license.checklist_url);
                            licenses.add(lic);
                        } else {
                            LPVSLicense newLicense = new LPVSLicense();
                            newLicense.setSpdxId(license.name);
                            newLicense.setLicenseName(license.name);
                            newLicense.setAccess("UNREVIEWED");
                            newLicense.setChecklistUrl(license.checklist_url);
                            newLicense.setAlternativeNames(null);
                            newLicense = lpvsLicenseRepository.save(newLicense);
                            licenseService.addLicenseToList(newLicense);
                            licenses.add(newLicense);
                        }

                        // Check for the license conflicts if the property "license_conflict=scanner"
                        if (licenseService.licenseConflictsSource.equalsIgnoreCase("scanner")) {
                            if (license.incompatible_with != null) {
                                for (String incompatibleLicense : license.incompatible_with) {
                                    licenseService.addLicenseConflict(incompatibleLicense, license.name);
                                }
                            }
                        }
                    }
                }
                file.setLicenses(new HashSet<>(licenses));
                if (!file.getLicenses().isEmpty()) detectedFiles.add(file);
            }

            // close reader
            reader.close();
        } catch (IOException ex) {
            log.error(ex.toString());
        }
        return detectedFiles;
    }

    // Scanoss JSON structure
    @SuppressFBWarnings(value = {"UUF_UNUSED_FIELD", "SIC_INNER_SHOULD_BE_STATIC"}, justification = "Parser class for Json deserialization")
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
