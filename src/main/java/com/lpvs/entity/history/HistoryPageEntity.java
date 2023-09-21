/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity.history;
import com.lpvs.entity.LPVSPullRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter @AllArgsConstructor
public class HistoryPageEntity {
    private Page<LPVSPullRequest> prPage;
    private Long count;
}
