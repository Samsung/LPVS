/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LPVSTableConfiguration {
    @Value("${app.table.detectedLicenseName}")
    private String detectedLicenseName;

    @Value("${app.table.detectedLicenseSchema}")
    private String detectedLicenseSchema;

    @Value("${app.table.diffFileName}")
    private String diffFileName;

    @Value("${app.table.pullRequestsName}")
    private String pulLRequestsName;

    @Value("${app.table.queueName}")
    private String queueName;

    // Configuration for detected license name
    public String getDetectedLicenseName() {
        return detectedLicenseName;
    }

    // Configuration for detected license schema
    public String getDetectedLicenseSchema() {
        return detectedLicenseSchema;
    }

    // Configuration for diff file name table
    public String getDiffFileName() {
        return diffFileName;
    }

    // Configuration for pull request name table
    public String getPullRequestsName() {
        return pulLRequestsName;
    }

    // Configurations for queue name
    public String getQueueName() {
        return queueName;
    }
}
