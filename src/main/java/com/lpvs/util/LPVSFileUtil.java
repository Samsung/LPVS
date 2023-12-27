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

/**
 * Utility class providing methods for handling files, directories, and file-related operations
 * in the context of LPVS application processing.
 * It includes functionality to save files, generate pretty patches, and manage local directory paths.
 */
@Slf4j
public class LPVSFileUtil {

    /**
     * Saves a file with the specified content in a given directory.
     *
     * @param fileName      The name of the file to be saved.
     * @param directoryPath The path to the directory where the file will be saved.
     * @param patchedLines  The content to be written to the file.
     */
    public static void saveFile(String fileName, String directoryPath, List<String> patchedLines) {
        try {
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

    /**
     * Saves the GitHub pull request file details, including the file patches, to a local directory.
     *
     * @param files          The iterable of GitHub pull request file details.
     * @param webhookConfig  The {@link LPVSQueue} configuration for the webhook.
     * @return The path to the directory where the files are saved.
     */
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

    /**
     * Deletes the specified directory if it exists.
     *
     * @param path The path of the directory to be deleted.
     */
    public static void deleteIfExists(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            FileSystemUtils.deleteRecursively(dir);
        }
    }

    /**
     * Retrieves the local directory path based on the provided webhook configuration.
     *
     * @param webhookConfig The {@link LPVSQueue} configuration for the webhook.
     * @return The local directory path.
     */
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

    /**
     * Retrieves the file path for storing scan results in JSON format based on the provided webhook configuration.
     *
     * @param webhookConfig The {@link LPVSQueue} configuration for the webhook.
     * @return The file path for storing scan results in JSON format.
     */
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

    /**
     * Retrieves the directory path for storing scan results based on the provided webhook configuration.
     *
     * @param webhookConfig The {@link LPVSQueue} configuration for the webhook.
     * @return The directory path for storing scan results.
     */
    public static String getScanResultsDirectoryPath(LPVSQueue webhookConfig) {
        return System.getProperty("user.home")
                + "/"
                + "Results/"
                + LPVSWebhookUtil.getRepositoryName(webhookConfig);
    }

    /**
     * Retrieves the local directory path for a given LPVSQueue configuration.
     *
     * @param webhookConfig LPVSQueue configuration.
     * @return Local directory path for the given LPVSQueue.
     */
    public static String getPathByPullRequest(LPVSQueue webhookConfig) {
        if (webhookConfig == null) return null;
        return getLocalDirectoryPath(webhookConfig);
    }
}
