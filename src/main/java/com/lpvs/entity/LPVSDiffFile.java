/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter @Setter
public class LPVSDiffFile {
    private String oldFileName;
    private String newFileName;
    private List<String> changedLines;

    public void appendPatchedLine(String line){
        if (this.changedLines == null) this.changedLines = new LinkedList<>();
        this.changedLines.add(line);
    }
}
