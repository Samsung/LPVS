/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

import org.junit.jupiter.api.*;
import java.util.*;

public class LPVSFileTestFuzzer {

    @BeforeEach
    void setUp() {
        lpvsFile = new LPVSFile();
    }

    @FuzzTest
    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        int number = data.consumeInt();
        String string = data.consumeRemainingAsString();
        // ...
        try {
            // do stuff here that possibly throw the exception
            return;
        } catch (IllegalStateException e) {
            return;
        }
    }
}
