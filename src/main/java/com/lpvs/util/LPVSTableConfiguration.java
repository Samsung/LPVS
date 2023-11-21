/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Component class providing configuration values for various database table names in the LPVS application.
 * This class is responsible for managing configuration properties related to database tables.
 */
@Component
@Getter
public class LPVSTableConfiguration {
    /**
     * Configuration property for the detected license table.
     */
    @Value("${app.table.detectedLicenseName}")
    private String detectedLicenseName;

    /**
     * Configuration property for the detected license schema table.
     */
    @Value("${app.table.detectedLicenseSchema}")
    private String detectedLicenseSchema;

    /**
     * Configuration property for the diff file table.
     */
    @Value("${app.table.diffFileName}")
    private String diffFileName;

    /**
     * Configuration property for the pull request table.
     */
    @Value("${app.table.pullRequestsName}")
    private String pullRequestsName;

    /**
     * Configuration property for the queue name table.
     */
    @Value("${app.table.queueName}")
    private String queueName;
}
