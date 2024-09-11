/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LPVSDiffFileTest {

    @Test
    public void testAppendPatchedLine() {
        LPVSDiffFile diffFile = new LPVSDiffFile();
        String line1 = "Line 1";
        String line2 = "Line 2";
        diffFile.appendPatchedLine(line1);
        diffFile.appendPatchedLine(line2);
        List<String> changedLines = diffFile.getChangedLines();
        assertNotNull(changedLines);
        assertEquals(2, changedLines.size());
        assertEquals(line1, changedLines.get(0));
        assertEquals(line2, changedLines.get(1));
    }

    @Test
    public void testAppendPatchedLineWithNullChangedLines() {
        LPVSDiffFile diffFile = new LPVSDiffFile();
        String line = "Line 1";
        diffFile.appendPatchedLine(line);
        List<String> changedLines = diffFile.getChangedLines();
        assertNotNull(changedLines);
        assertEquals(1, changedLines.size());
        assertEquals(line, changedLines.get(0));
    }

    @Test
    public void testAppendPatchedLineWithEmptyChangedLines() {
        LPVSDiffFile diffFile = new LPVSDiffFile();
        diffFile.setChangedLines(new LinkedList<>());
        String line = "Line 1";
        diffFile.appendPatchedLine(line);
        List<String> changedLines = diffFile.getChangedLines();
        assertNotNull(changedLines);
        assertEquals(1, changedLines.size());
        assertEquals(line, changedLines.get(0));
    }
}
