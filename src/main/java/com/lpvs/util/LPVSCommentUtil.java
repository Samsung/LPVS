/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.util;

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
        if (webhookConfig == null) {
            return file.getMatchedLines();
        }
        String prefix =
                LPVSPayloadUtil.getRepositoryUrl(webhookConfig)
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
                                "<a target=\"_blank\" href=\""
                                        + link
                                        + "\">"
                                        + lineInfo
                                        + "</a>  ");
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

        StringBuilder commitCommentBuilder = new StringBuilder();

        if (scanResults != null && scanResults.size() != 0) {
            commitCommentBuilder.append("**Detected licenses:**\n\n\n");
            for (LPVSFile file : scanResults) {
                commitCommentBuilder.append("**File:** ");
                commitCommentBuilder.append(file.getFilePath());
                commitCommentBuilder.append("\n");
                commitCommentBuilder.append("**License(s):** ");
                commitCommentBuilder.append(file.convertLicensesToString(LPVSVcs.GITHUB));
                commitCommentBuilder.append("\n");
                commitCommentBuilder.append("**Component:** ");
                commitCommentBuilder.append(file.getComponentName());
                commitCommentBuilder.append(" (");
                commitCommentBuilder.append(file.getComponentFilePath());
                commitCommentBuilder.append(")\n");
                commitCommentBuilder.append("**Matched Lines:** ");
                commitCommentBuilder.append(
                        LPVSCommentUtil.getMatchedLinesAsLink(webhookConfig, file, LPVSVcs.GITHUB));
                commitCommentBuilder.append("\n");
                commitCommentBuilder.append("**Snippet Match:** ");
                commitCommentBuilder.append(file.getSnippetMatch());
                commitCommentBuilder.append("\n\n\n\n");
            }
        }

        if (conflicts != null && conflicts.size() > 0) {
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
        }

        return commitCommentBuilder.toString();
    }
}
