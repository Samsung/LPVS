/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.lpvs.entity.LPVSDetectedLicense;
import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.entity.enums.LPVSVcs;
import com.lpvs.service.LPVSLicenseService;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class providing methods for generating links and formatting matched lines in comments.
 * It includes functionality to create links to specific lines in a file on version control platforms
 * like GitHub, based on the provided {@link LPVSQueue} configuration, {@link LPVSFile}, and {@link LPVSVcs}.
 *
 * <p>
 * This class is designed to enhance the generation of comment content by creating clickable links
 * to specific lines in a file, making it easier for users to navigate directly to the relevant code.
 * </p>
 */
@Slf4j
public class LPVSCommentUtil {

    /**
     * Generates a formatted string containing links to matched lines in a file.
     *
     * @param webhookConfig The {@link LPVSQueue} configuration for the webhook.
     * @param file           The {@link LPVSFile} representing the file with matched lines.
     * @param vcs            The {@link LPVSVcs} representing the version control system (e.g., GitHub).
     * @return A string containing formatted links to matched lines in the file.
     */
    public static String getMatchedLinesAsLink(
            LPVSQueue webhookConfig, LPVSFile file, LPVSVcs vcs) {
        String prefix =
                LPVSWebhookUtil.getRepositoryUrl(webhookConfig)
                        + "/blob/"
                        + webhookConfig.getHeadCommitSHA()
                        + "/"
                        + file.getFilePath();
        String matchedLines = "";
        if (file.getMatchedLines().equals("all")) {
            if (vcs != null && vcs.equals(LPVSVcs.GITHUB)) {
                return "<a target=\"_blank\" href=\""
                        + prefix
                        + "\">"
                        + file.getMatchedLines()
                        + "</a>";
            } else {
                return file.getMatchedLines() + " (" + prefix + ")";
            }
        }
        prefix = prefix.concat("#L");
        for (String lineInfo : file.getMatchedLines().split(",")) {
            String link = prefix + lineInfo.replace('-', 'L');
            if (vcs != null && vcs.equals(LPVSVcs.GITHUB)) {
                matchedLines =
                        matchedLines.concat(
                                "<a target=\"_blank\" href=\"" + link + "\">" + lineInfo + "</a>");
            } else {
                matchedLines = matchedLines.concat(lineInfo + " (" + link + ") ");
            }
        }
        log.debug("MatchedLines: " + matchedLines);
        return matchedLines;
    }

    /**
     * Generates a formatted string for an LPVS GitHub comment.
     *
     * @param webhookConfig The {@link LPVSQueue} configuration for the webhook.
     * @param scanResults   List containing preformatted scan results.
     * @param conflicts     List of conflicts, containing license conflict information.
     * @return A string containing scan results in GitHub-friendly format.
     */
    public static String reportCommentBuilder(
            LPVSQueue webhookConfig,
            List<LPVSFile> scanResults,
            List<LPVSLicenseService.Conflict<String, String>> conflicts) {
        String commitComment = "";

        if (scanResults != null && scanResults.size() != 0) {
            commitComment = "**Detected licenses:**\n\n\n";
            for (LPVSFile file : scanResults) {
                commitComment += "**File:** " + file.getFilePath() + "\n";
                commitComment +=
                        "**License(s):** " + file.convertLicensesToString(LPVSVcs.GITHUB) + "\n";
                commitComment +=
                        "**Component:** "
                                + file.getComponentName()
                                + " ("
                                + file.getComponentFilePath()
                                + ")\n";
                commitComment +=
                        "**Matched Lines:** "
                                + LPVSCommentUtil.getMatchedLinesAsLink(
                                        webhookConfig, file, LPVSVcs.GITHUB)
                                + "\n";
                commitComment += "**Snippet Match:** " + file.getSnippetMatch() + "\n\n\n\n";
            }
        }

        if (conflicts != null && conflicts.size() > 0) {
            StringBuilder commitCommentBuilder = new StringBuilder();
            commitCommentBuilder.append("**Detected license conflicts:**\n\n\n");
            commitCommentBuilder.append("<ul>");
            for (LPVSLicenseService.Conflict<String, String> conflict : conflicts) {
                commitCommentBuilder.append("<li>" + conflict.l1 + " and " + conflict.l2 + "</li>");
                LPVSDetectedLicense detectedIssue = new LPVSDetectedLicense();
                detectedIssue.setIssue(true);
            }
            commitCommentBuilder.append("</ul>");
            if (null != webhookConfig.getHubLink()) {
                commitCommentBuilder.append("(");
                commitCommentBuilder.append(webhookConfig.getHubLink());
                commitCommentBuilder.append(")");
            }
            commitComment += commitCommentBuilder.toString();
        }

        return commitComment;
    }

    /**
     * Generates a formatted string for an HTML report with scan results.
     *
     * @param webhookConfig The {@link LPVSQueue} configuration for the webhook.
     * @param scanResults   List containing preformatted scan results.
     * @param conflicts     List containing license conflict information.
     * @return A string containing scan results in HTML format.
     */
    public static String buildHTMLComment(
            LPVSQueue webhookConfig,
            List<LPVSFile> scanResults,
            List<LPVSLicenseService.Conflict<String, String>> conflicts) {
        StringBuilder htmlBuilder = new StringBuilder();

        htmlBuilder.append("<html><body>");

        if (scanResults != null && scanResults.size() != 0) {
            htmlBuilder.append("<h2>Detected licenses:</h2>");
            for (LPVSFile file : scanResults) {
                htmlBuilder
                        .append("<p><strong>File:</strong> ")
                        .append(file.getFilePath())
                        .append("</p>");
                htmlBuilder
                        .append("<p><strong>License(s):</strong> ")
                        .append(file.convertLicensesToString(LPVSVcs.GITHUB))
                        .append("</p>");
                htmlBuilder
                        .append("<p><strong>Component:</strong> ")
                        .append(file.getComponentName())
                        .append(" (")
                        .append(file.getComponentFilePath())
                        .append(")</p>");
                htmlBuilder
                        .append("<p><strong>Matched Lines:</strong> ")
                        .append(
                                LPVSCommentUtil.getMatchedLinesAsLink(
                                        webhookConfig, file, LPVSVcs.GITHUB))
                        .append("</p>");
                htmlBuilder
                        .append("<p><strong>Snippet Match:</strong> ")
                        .append(file.getSnippetMatch())
                        .append("</p>");
                htmlBuilder.append("<hr>");
            }
        }

        if (conflicts != null && conflicts.size() > 0) {
            htmlBuilder.append("<h2>Detected license conflicts:</h2>");
            htmlBuilder.append("<ul>");
            for (LPVSLicenseService.Conflict<String, String> conflict : conflicts) {
                htmlBuilder
                        .append("<li>")
                        .append(conflict.l1)
                        .append(" and ")
                        .append(conflict.l2)
                        .append("</li>");
            }
            htmlBuilder.append("</ul>");
            if (webhookConfig.getHubLink() != null) {
                htmlBuilder.append("<p>").append(webhookConfig.getHubLink()).append("</p>");
            }
        }

        htmlBuilder.append("</body></html>");

        return htmlBuilder.toString();
    }

    /**
     * Saves HTML report to given location.
     *
     * @param htmlContent   The string, containing report in HTML format.
     * @param filePath      The path to expected html report file.
     */
    public static void saveHTMLToFile(String htmlContent, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(htmlContent);
            log.info("LPVS report saved to: " + filePath);
        } catch (IOException ex) {
            log.error("error during saving HTML report: " + ex);
        }
    }
}
