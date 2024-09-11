/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a file diff in the LPVS system.
 */
@Getter
@Setter
public class LPVSDiffFile {
    /**
     * The old file name before changes.
     */
    private String oldFileName;

    /**
     * The new file name after changes.
     */
    private String newFileName;

    /**
     * List of lines that have been changed in the file.
     */
    private List<String> changedLines;

    /**
     * Appends a patched line to the list of changed lines.
     *
     * @param line The line to be appended.
     */
    public void appendPatchedLine(String line) {
        if (this.changedLines == null) this.changedLines = new LinkedList<>();
        this.changedLines.add(line);
    }
}
