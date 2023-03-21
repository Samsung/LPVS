/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service;

import com.lpvs.entity.LPVSQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Slf4j
public class LPVSQueueProcessorService {

    private LPVSQueueService queueService;

    @Autowired
    LPVSQueueProcessorService(LPVSQueueService queueService) {
        this.queueService = queueService;
    }

    @EventListener(ApplicationReadyEvent.class)
    private void queueProcessor() throws Exception {
        queueService.checkForQueue();
        while (true) {
            LPVSQueue webhookConfig = queueService.getQueueFirstElement();
            log.info("PROCESS Webhook id = " + webhookConfig.getId());
            webhookConfig.setDate(new Date());
            queueService.processWebHook(webhookConfig);
        }
    }
}
