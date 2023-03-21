/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.util;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.entity.enums.LPVSVcs;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LPVSCommentUtil {

    public static String getMatchedLinesAsLink(LPVSQueue webhookConfig, LPVSFile file, LPVSVcs vcs) {
        String prefix = LPVSWebhookUtil.getRepositoryUrl(webhookConfig) + "/blob/" + webhookConfig.getHeadCommitSHA() + "/" + file.getFilePath();
        String matchedLines = new String();
        if (file.getMatchedLines().equals("all")) {
            if (vcs != null && vcs.equals(LPVSVcs.GITHUB)) {
                return "<a target=\"_blank\" href=\"" + prefix + "\">" + file.getMatchedLines() + "</a>";
            } else {
                return file.getMatchedLines() + " (" + prefix + ")";
            }
        }
        prefix = prefix.concat("#L");
        for (String lineInfo : file.getMatchedLines().split(",")){
            String link = prefix+lineInfo.replace('-','L');
            if (vcs != null && vcs.equals(LPVSVcs.GITHUB)) {
                matchedLines = matchedLines.concat("<a target=\"_blank\" href=\"" + link + "\">" + lineInfo + "</a>");
            } else {
                matchedLines = matchedLines.concat(lineInfo + " (" + link + ")");
            }
        }
        log.debug("MatchedLines: " + matchedLines);
        return matchedLines;
    }
}
