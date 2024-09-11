/**
 * Copyright (c) 2023, Basaeng, kyudori, hwan5180, quswjdgma83
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.history;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Represents an entity containing a list of historical records in the LPVS system.
 * Each instance of this class encapsulates a collection of {@link LPVSHistory} objects and a count representing
 * the total number of historical records.
 * <p>
 * This entity is used to organize and provide access to historical scan data in the LPVS system.
 * The list of {@link LPVSHistory} objects contains detailed information about individual scan events.
 * The count field indicates the total number of historical records available in the entity.
 * </p>
 */
@Getter
@AllArgsConstructor
public class HistoryEntity {

    /**
     * The list of {@link LPVSHistory} objects representing individual historical records.
     */
    private List<LPVSHistory> lpvsHistories;

    /**
     * The total number of historical records in the entity.
     */
    private Long count;
}
