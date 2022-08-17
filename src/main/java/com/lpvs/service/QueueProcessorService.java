/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service;

import com.lpvs.entity.config.WebhookConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class QueueProcessorService {

    @Autowired
    private QueueService queueService;

    private static Logger LOG = LoggerFactory.getLogger(QueueProcessorService.class);

    @EventListener(ApplicationReadyEvent.class)
    private void queueProcessor() throws Exception {
        while (true) {
            WebhookConfig webhookConfig = queueService.getQueueFirstElement();
            LOG.info("PROCESS Webhook id = " + webhookConfig.getWebhookId());
            webhookConfig.setDate(new Date());
            queueService.processWebHook(webhookConfig);

        }
    }
}
