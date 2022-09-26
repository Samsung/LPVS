/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.config.WebhookConfig;
import com.lpvs.service.scanner.scanoss.ScanossDetectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class DetectService {

    private String scannerType;

    private ScanossDetectService scanossDetectService;

    @Autowired
    public DetectService(@Value("${scanner:scanoss}") String scannerType,
                         ScanossDetectService scanossDetectService) {
        this.scannerType = scannerType;
        this.scanossDetectService = scanossDetectService;
    }

    private static final Logger LOG = LoggerFactory.getLogger(DetectService.class);

    @PostConstruct
    private void init() {
        LOG.info("License detection scanner: " + scannerType);
    }

    public List<LPVSFile> runScan(WebhookConfig webhookConfig, String path) throws Exception {
        if (scannerType.equals("scanoss")) {
            scanossDetectService.runScan(webhookConfig, path);
            return scanossDetectService.checkLicenses(webhookConfig);
        }
        return new ArrayList<>();
    }
}
