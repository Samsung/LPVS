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
import org.springframework.web.util.HtmlUtils;

import java.util.Locale;

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
     * Escapes special HTML characters in the given value to prevent HTML injection.
     *
     * @param value The value to escape.
     * @return The escaped string representation of the value ("null" if the value is null).
     */
    public static String escapeHtml(Object value) {
        return HtmlUtils.htmlEscape(String.valueOf(value));
    }

    /**
     * Checks whether the URL can be safely used as a link, i.e. it uses the http or https scheme.
     *
     * @param url The URL to check.
     * @return true if the URL uses the http or https scheme, false otherwise.
     */
    public static boolean isSafeUrl(String url) {
        if (url == null) {
            return false;
        }
        String lowerCaseUrl = url.trim().toLowerCase(Locale.ROOT);
        return lowerCaseUrl.startsWith("http://") || lowerCaseUrl.startsWith("https://");
    }

    /**
     * Generates an HTML link with escaped URL and text.
     * If the URL doesn't use the http or https scheme, only the escaped text is returned.
     *
     * @param url     The URL of the link.
     * @param text    The text of the link.
     * @param newTab  Whether the link should be opened in a new tab.
     * @return The HTML code of the link, or the escaped text if the URL is not safe.
     */
    public static String getHtmlLink(String url, Object text, boolean newTab) {
        if (!isSafeUrl(url)) {
            return escapeHtml(text);
        }
        return "<a "
                + (newTab ? "target=\"_blank\" " : "")
                + "href=\""
                + escapeHtml(url.trim())
                + "\">"
                + escapeHtml(text)
                + "</a>";
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
            return escapeHtml(file.getMatchedLines());
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
                return getHtmlLink(prefix, file.getMatchedLines(), true);
            } else {
                return file.getMatchedLines() + " (" + prefix + ")";
            }
        }
        prefix = prefix.concat("#L");
        for (String lineInfo : file.getMatchedLines().split(",")) {
            String link = prefix + lineInfo.replace('-', 'L');
            if (vcs != null && vcs.equals(LPVSVcs.GITHUB)) {
                matchedLines = matchedLines.concat(getHtmlLink(link, lineInfo, true) + "  ");
            } else {
                matchedLines = matchedLines.concat(lineInfo + " (" + link + ") ");
            }
        }
        log.debug("MatchedLines: " + matchedLines);
        return matchedLines;
    }
}
