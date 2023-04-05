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

@Getter
@Setter
public class LPVSDiffFile {
    private String originalFile;
    private String newFile;
    private List<String> addedLines;
    private List<String> deletedLines;
    private List<String> unchangedLines;

    public void appendAddedLine(String line){
        if (this.addedLines == null) this.addedLines = new LinkedList<>();
        this.addedLines.add(line);
    }

    public void appendDeletedLine(String line){
        if (this.deletedLines == null) this.deletedLines = new LinkedList<>();
        this.deletedLines.add(line);
    }

    public void appendUnchangedLine(String line){
        if (this.unchangedLines == null) this.unchangedLines = new LinkedList<>();
        this.unchangedLines.add(line);
    }
}
