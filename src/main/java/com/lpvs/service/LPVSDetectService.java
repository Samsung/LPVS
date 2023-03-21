/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.service.scanner.scanoss.LPVSScanossDetectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LPVSDetectService {

    private String scannerType;

    private LPVSScanossDetectService scanossDetectService;

    @Autowired
    public LPVSDetectService(@Value("${scanner:scanoss}") String scannerType,
                             LPVSScanossDetectService scanossDetectService) {
        this.scannerType = scannerType;
        this.scanossDetectService = scanossDetectService;
    }

    @PostConstruct
    private void init() {
        log.info("License detection scanner: " + scannerType);
    }

    public List<LPVSFile> runScan(LPVSQueue webhookConfig, String path) throws Exception {
        if (scannerType.equals("scanoss")) {
            scanossDetectService.runScan(webhookConfig, path);
            return scanossDetectService.checkLicenses(webhookConfig);
        }
        return new ArrayList<>();
    }
}
