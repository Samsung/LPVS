/**
 * Copyright (c) 2023, Basaeng, kyudori, hwan5180, quswjdgma83
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.history;

import com.lpvs.entity.LPVSPullRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

/**
 * Represents an entity containing a page of historical pull requests in the LPVS system.
 * Each instance of this class encapsulates a {@link Page} of {@link LPVSPullRequest} objects and a count representing
 * the total number of pull requests.
 * <p>
 * This entity is used to organize and provide access to historical pull request data in the LPVS system.
 * The {@link Page} of {@link LPVSPullRequest} objects represents a subset of pull requests for a specific page,
 * and the count field indicates the total number of pull requests available in the entity.
 * </p>
 */
@Getter
@AllArgsConstructor
public class HistoryPageEntity {

    /**
     * The {@link Page} of {@link LPVSPullRequest} objects representing a subset of historical pull requests.
     */
    private Page<LPVSPullRequest> prPage;

    /**
     * The total number of historical pull requests in the entity.
     */
    private Long count;
}
