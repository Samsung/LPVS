/**
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service.scan;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSQueue;

import java.util.List;

/**
 * Interface representing a service for scanning files and checking licenses.
 */
public interface LPVSScanService {

    /**
     * Runs a scan on the specified path using the provided webhook configuration.
     *
     * @param webhookConfig The webhook configuration to use for the scan.
     * @param path          The path to the file or directory to scan.
     * @throws Exception if an error occurs during the scan process.
     */
    void runScan(LPVSQueue webhookConfig, String path) throws Exception;

    /**
     * Checks licenses for files using the provided webhook configuration.
     *
     * @param webhookConfig The webhook configuration to use for checking licenses.
     * @return A list of LPVSFile objects representing files with detected licenses.
     */
    List<LPVSFile> checkLicenses(LPVSQueue webhookConfig);
}
