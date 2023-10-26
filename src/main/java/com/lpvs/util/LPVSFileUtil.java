/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.util;

import com.lpvs.entity.LPVSQueue;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class LPVSFileUtil {

    public static void saveFile(String fileName, String directoryPath, List<String> patchedLines) {
        try {
            if (patchedLines == null || patchedLines.size() == 0) {
                log.error("NULL PATCH for file " + fileName);
                return;
            }

            int cnt = 1;
            StringBuilder prettyPatch = new StringBuilder();
            for (String patchedLine : patchedLines) {
                // empty line
                if (patchedLine.isEmpty()) {
                    prettyPatch.append("\n");
                    cnt++;
                }
                // added and unchanged lines
                else if (patchedLine.charAt(0) == '+' || patchedLine.charAt(0) == ' ') {
                    prettyPatch.append(patchedLine.substring(1));
                    prettyPatch.append("\n");
                    cnt++;
                }
                // information(location, number of lines) about changed lines
                else if (patchedLine.charAt(0) == '@') {
                    int fIndex = patchedLine.indexOf("+") + 1;
                    int lIndex = patchedLine.indexOf(',', fIndex);
                    if (lIndex == -1) lIndex = patchedLine.indexOf(' ', fIndex);
                    int startLine = Integer.parseInt(patchedLine.substring(fIndex, lIndex));
                    log.debug("Line from: " + startLine + " Git string: " + patchedLine);
                    for (int i = cnt; i < startLine; i++) {
                        prettyPatch.append("\n");
                    }
                    cnt = startLine;
                }
            }

            if (fileName.contains("/")) {
                String filepath = fileName.substring(0, fileName.lastIndexOf("/"));
                Path resultFolder = Paths.get(directoryPath + "/" + filepath);
                if (!Files.exists(resultFolder)) {
                    try {
                        // create folder
                        Files.createDirectories(resultFolder);
                        log.debug("Folder created successfully.");
                    } catch (IOException e) {
                        log.error("Failed to create folder " + resultFolder + e);
                    }
                } else {
                    log.debug("Folder already exists.");
                }
            }
            if (prettyPatch.length() > 0) {
                try (FileWriter fileWriter =
                                new FileWriter(
                                        directoryPath + "/" + fileName, Charset.forName("UTF8"));
                        BufferedWriter writer = new BufferedWriter(fileWriter)) {
                    writer.write(prettyPatch.toString());
                }
            }
        } catch (IOException e) {
            log.error("Error while writing file " + fileName + ": " + e.getMessage());
        }
    }

    public static String saveGithubDiffs(
            Iterable<GHPullRequestFileDetail> files, LPVSQueue webhookConfig) {
        String directoryPath = LPVSFileUtil.getLocalDirectoryPath(webhookConfig);
        deleteIfExists(directoryPath);
        boolean result = new File(directoryPath).mkdirs();
        if (result) {
            for (GHPullRequestFileDetail file : files) {
                if (file.getPatch() != null) {
                    List<String> patch = Arrays.asList(file.getPatch().split("\n"));
                    saveFile(file.getFilename(), directoryPath, patch);
                } else {
                    log.error("NULL PATCH for file " + file.getFilename());
                }
            }
        }
        return directoryPath;
    }

    public static void deleteIfExists(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            FileSystemUtils.deleteRecursively(dir);
        }
    }

    public static String getLocalDirectoryPath(LPVSQueue webhookConfig) {
        if (webhookConfig.getHeadCommitSHA() == null
                || webhookConfig.getHeadCommitSHA().equals("")) {
            return System.getProperty("user.home")
                    + "/"
                    + "Projects/"
                    + LPVSWebhookUtil.getRepositoryName(webhookConfig)
                    + "/"
                    + LPVSWebhookUtil.getPullRequestId(webhookConfig);
        } else {
            return System.getProperty("user.home")
                    + "/"
                    + "Projects/"
                    + LPVSWebhookUtil.getRepositoryName(webhookConfig)
                    + "/"
                    + webhookConfig.getHeadCommitSHA();
        }
    }

    public static String getScanResultsJsonFilePath(LPVSQueue webhookConfig) {
        if (webhookConfig.getHeadCommitSHA() == null
                || webhookConfig.getHeadCommitSHA().equals("")) {
            return System.getProperty("user.home")
                    + "/"
                    + "Results/"
                    + LPVSWebhookUtil.getRepositoryName(webhookConfig)
                    + "/"
                    + LPVSWebhookUtil.getPullRequestId(webhookConfig)
                    + ".json";
        } else {
            return System.getProperty("user.home")
                    + "/"
                    + "Results/"
                    + LPVSWebhookUtil.getRepositoryName(webhookConfig)
                    + "/"
                    + webhookConfig.getHeadCommitSHA()
                    + ".json";
        }
    }

    public static String getScanResultsDirectoryPath(LPVSQueue webhookConfig) {
        return System.getProperty("user.home")
                + "/"
                + "Results/"
                + LPVSWebhookUtil.getRepositoryName(webhookConfig);
    }
}
