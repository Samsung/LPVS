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
import com.lpvs.entity.config.WebhookConfig;
import com.lpvs.service.GitHubService;
import com.lpvs.service.LicenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class ScanossDetectService {

    @Autowired
    private LicenseService licenseService;

    @Autowired
    private GitHubService gitHubService;

    private static final Logger LOG = LoggerFactory.getLogger(ScanossDetectService.class);

    public void runScan(WebhookConfig webhookConfig, String path) throws Exception {
        LOG.info("Starting Scanoss scanning");

        try {
            ProcessBuilder processBuilder;
            if (!(new File("RESULTS").exists())) {
                new File("RESULTS").mkdir();
            }
            processBuilder = new ProcessBuilder(
                "scanoss-py", "scan",
                "-t",
                "--no-wfp-output",
                "-o", "RESULTS/" + webhookConfig.getRepositoryName() + "_" + webhookConfig.getHeadCommitSHA() + ".json",
                path
            );
            Process process = processBuilder.inheritIO().start();

            int status = process.waitFor();

            if (status == 1) {
                LOG.error("Scanoss scanner terminated with none-zero code. Terminating.");
                BufferedReader output = null;
                try {
                    output = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    LOG.error(output.readLine());
                    throw new Exception("Scanoss scanner terminated with none-zero code. Terminating.");
                }
                finally {
                    if(output != null)
                        output.close();
                }
            }
        } catch (IOException | InterruptedException ex) {
            LOG.error("Scanoss scanner terminated with none-zero code. Terminating.");
            throw ex;
        }

        LOG.info("Scanoss scan done");
    }

    public List<LPVSFile> checkLicenses(WebhookConfig webhookConfig) {
        List<LPVSFile> detectedFiles = new ArrayList<>();
        try {
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(Paths.get("RESULTS/" + webhookConfig.getRepositoryName() + "_" + webhookConfig.getHeadCommitSHA() + ".json"));
            // convert JSON file to map
            Map<String, ArrayList<Object>> map = gson.fromJson(reader,
                    new TypeToken<Map<String, ArrayList<Object>>>() {}.getType());

            // parse map entries
            long ind = 0L;
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
                        .replaceAll("incompatible_with\" : (\".*?\"), \"name", "incompatible_with\" : \\[$1\\], \"name")
                        ;
                content = content.substring(1, content.length() - 1);
                if(content.endsWith("}"))
                {
                    content = content.substring(0, content.length() - 1) + "\"}";
                }
                content = content.replaceAll("}\"}", "\"}}");
                ScanossJsonStructure object = gson.fromJson(content, ScanossJsonStructure.class);
                if (object.file_url != null) file.setFileUrl(object.file_url);
                if (object.component != null) file.setComponent(object.component);
                if (object.lines != null) file.setMatchedLines(object.lines);
                if (object.matched != null) file.setSnippetMatch(object.matched);

                Set<LPVSLicense> licenses = new HashSet<>();
                if (object.licenses != null) {
                    for (ScanossJsonStructure.ScanossLicense license : object.licenses) {
                        // Check detected licenses
                        LPVSLicense lic = licenseService.findLicenseBySPDX(license.name);
                        if (lic != null) {
                            lic.setChecklist_url(license.checklist_url);
                            licenses.add(lic);
                        } else {
                            licenses.add(new LPVSLicense(0L, license.name, license.name, "UNREVIEWED", license.checklist_url, new ArrayList<>()));
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
        } catch (Exception ex) {
            LOG.error(ex.toString());
        }
        return detectedFiles;
    }

    // Scanoss JSON structure
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
