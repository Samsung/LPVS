/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.dashboard;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

/**
 * Represents a collection of dashboard elements in the LPVS system.
 * This class includes a map associating each date with corresponding {@link DashboardElementsByDate}.
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class DashBoardElements {

    /**
     * The map associating each date with corresponding dashboard elements.
     */
    private Map<LocalDate, DashboardElementsByDate> dashboardElementsMap;
}
