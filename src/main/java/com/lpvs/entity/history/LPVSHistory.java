/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity.history;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @AllArgsConstructor
public class LPVSHistory {
    private String scanDate;
    private String repositoryName;
    private Long pullRequestId;
    private String url;
    private String status;
    private String sender;
    private String pullNumber; // pull/number
    private Boolean hasIssue;
}
