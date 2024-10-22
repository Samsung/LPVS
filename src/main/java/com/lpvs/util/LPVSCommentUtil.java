/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.util;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.entity.enums.LPVSVcs;

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
     * Private constructor to prevent instantiation of utility class
     */
    private LPVSCommentUtil() {
        throw new UnsupportedOperationException("Utility class, cannot be instantiated.");
    }

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
}
