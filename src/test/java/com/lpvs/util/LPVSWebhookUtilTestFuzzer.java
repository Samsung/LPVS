/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.util;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.lpvs.entity.LPVSQueue;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

public class LPVSWebhookUtilTestFuzzer {

    @FuzzTest(maxDuration = "60s")
    @SetEnvironmentVariable(key = "JAZZER_FUZZ", value = "1")
    public void fuzzTestGetPullRequestId(FuzzedDataProvider data) {
        System.setProperty("JAZZER_FUZZ", "1");

        LPVSQueue webhookConfig = new LPVSQueue();

        // Fuzzing input data
        webhookConfig.setRepositoryUrl(data.consumeRemainingAsString());
        webhookConfig.setPullRequestUrl(data.consumeRemainingAsString());

        // Perform the fuzz test
        try {
            String result = LPVSWebhookUtil.getPullRequestId(webhookConfig);

            // Validate the result
            assertNotNull(result);
        } catch (Exception e) {
            // Handle exceptions caused by unexpected inputs gracefully
            // Log the exception or perform other actions
            fail("| Unexpected exception: --- | " + e.getMessage());
        }
    }
}
