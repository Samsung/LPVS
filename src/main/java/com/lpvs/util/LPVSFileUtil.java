/**
 * Copyright (c) 2022-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.util;

import com.lpvs.entity.LPVSQueue;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.util.HtmlUtils;
import io.micrometer.common.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
     * Private constructor to prevent instantiation of utility class
     */
    private LPVSFileUtil() {
        throw new UnsupportedOperationException("Utility class, cannot be instantiated.");
    }

    /**
     * Saves a file with the specified content in a given directory.
     *
     * @param fileName      The name of the file to be saved.
     * @param directoryPath The path to the directory where the file will be saved.
     * @param patchedLines  The content to be written to the file.
     * @return {@code true} if file is saved, {@code false} otherwise.
     */
    public static boolean saveFile(
            String fileName, String directoryPath, List<String> patchedLines) {
        if (patchedLines == null || patchedLines.isEmpty()) {
            log.error("Empty patch for file " + fileName);
            return false;
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

        if (!prettyPatch.isEmpty()) {
            try {
                saveFileToDisk(directoryPath, fileName, prettyPatch.toString());
            } catch (IOException e) {
                log.error(e.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * Saves the given content to a file with the specified filename in the provided directory.
     * @param localDirectoryPath The local directory path where the file should be saved.
     * @param fileName The name of the file to be saved.
     * @param content The content to be written to the file.
     * @throws IOException If there is an error while creating the folder or writing the file.
     */
    public static void saveFileToDisk(String localDirectoryPath, String fileName, String content)
            throws IOException {
        if (fileName.contains("/")) {
            String filepath = fileName.substring(0, fileName.lastIndexOf("/"));
            Path resultFolder = Paths.get(localDirectoryPath + File.separator + filepath);
            if (!Files.exists(resultFolder)) {
                try {
                    // create folder
                    Files.createDirectories(resultFolder);
                    log.debug("Folder created successfully.");
                } catch (IOException e) {
                    log.error(e.getMessage());
                    throw new IOException("Failed to create folder " + resultFolder);
                }
            } else {
                log.debug("Folder already exists.");
            }
        }

        try (FileWriter fileWriter =
                        new FileWriter(
                                localDirectoryPath + File.separator + fileName,
                                StandardCharsets.UTF_8);
                BufferedWriter writer = new BufferedWriter(fileWriter)) {
            writer.write(content);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new IOException("Error while writing file " + fileName);
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
     * Copies files from a source path to a destination directory path.
     *
     * @param sourcePath the path to the source file or directory to be copied
     * @param directoryPath the path to the destination directory where the files will be copied
     * @throws IOException if an I/O error occurs
     */
    public static void copyFiles(String sourcePath, String directoryPath) throws IOException {
        deleteIfExists(directoryPath);
        File destination = new File(directoryPath);
        File source = new File(sourcePath);
        if (destination.mkdirs()) {
            if (source.isDirectory()) {
                File[] files = source.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            Files.copy(
                                    file.toPath(),
                                    new File(destination, file.getName()).toPath(),
                                    StandardCopyOption.REPLACE_EXISTING);
                        } else if (file.isDirectory()) {
                            File destinationSubdir = new File(destination, file.getName());
                            boolean isCreated = destinationSubdir.mkdirs();
                            if (!isCreated) {
                                throw new IOException(
                                        "Failed to create directory: "
                                                + destinationSubdir.getAbsolutePath());
                            }
                            copyFiles(file.getAbsolutePath(), destinationSubdir.getAbsolutePath());
                        }
                    }
                }
            } else if (source.isFile()) {
                Files.copy(
                        source.toPath(),
                        new File(destination, source.getName()).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
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
        if (StringUtils.isBlank(webhookConfig.getHeadCommitSHA())) {
            return System.getProperty("user.home")
                    + File.separator
                    + "LPVS"
                    + File.separator
                    + "Projects"
                    + File.separator
                    + LPVSPayloadUtil.getRepositoryName(webhookConfig)
                    + File.separator
                    + webhookConfig.getId()
                    + "-"
                    + LPVSPayloadUtil.getPullRequestId(webhookConfig);
        } else {
            return System.getProperty("user.home")
                    + File.separator
                    + "LPVS"
                    + File.separator
                    + "Projects"
                    + File.separator
                    + LPVSPayloadUtil.getRepositoryName(webhookConfig)
                    + File.separator
                    + webhookConfig.getId()
                    + "-"
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
        String fileName = null;
        if (StringUtils.isBlank(webhookConfig.getHeadCommitSHA())) {
            fileName =
                    webhookConfig.getId() + "-" + LPVSPayloadUtil.getPullRequestId(webhookConfig);
        } else {
            fileName =
                    webhookConfig.getId()
                            + "-"
                            + HtmlUtils.htmlEscape(webhookConfig.getHeadCommitSHA());
        }

        return System.getProperty("user.home")
                + File.separator
                + "LPVS"
                + File.separator
                + "Results"
                + File.separator
                + LPVSPayloadUtil.getRepositoryName(webhookConfig)
                + File.separator
                + fileName
                + ".json";
    }

    /**
     * Retrieves the directory path for storing scan results based on the provided webhook configuration.
     *
     * @param webhookConfig The {@link LPVSQueue} configuration for the webhook.
     * @return The directory path for storing scan results.
     */
    public static String getScanResultsDirectoryPath(LPVSQueue webhookConfig) {
        return System.getProperty("user.home")
                + File.separator
                + "LPVS"
                + File.separator
                + "Results"
                + File.separator
                + LPVSPayloadUtil.getRepositoryName(webhookConfig);
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
