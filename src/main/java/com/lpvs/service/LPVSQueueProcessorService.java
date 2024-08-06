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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service responsible for processing LPVSQueue elements.
 */
@Service
@Slf4j
public class LPVSQueueProcessorService {

    /**
     * Service for managing LPVSQueue elements.
     */
    private LPVSQueueService queueService;

    /**
     * Trigger value to start a single scan of a pull request (optional).
     */
    @Value("${github.pull.request:}")
    private String trigger;

    /**
     * Trigger value to start a single scan of local files or folder (optional).
     */
    @Value("${local.path:}")
    private String localPath;

    /**
     * Constructor for LPVSQueueProcessorService.
     *
     * @param queueService The LPVSQueueService to be injected.
     */
    @Autowired
    LPVSQueueProcessorService(LPVSQueueService queueService) {
        this.queueService = queueService;
    }

    /**
     * Event listener method triggered when the application is ready.
     * It initiates the queue processing loop.
     *
     * @throws Exception If an exception occurs during queue processing.
     */
    @EventListener(ApplicationReadyEvent.class)
    protected void queueProcessor() throws Exception {
        // Check for any pending elements in the LPVSQueue.
        queueService.checkForQueue();

        // Process LPVSQueue elements until the trigger is set.
        while (trigger.isBlank() && localPath.isBlank()) {
            // Get the first element from the LPVSQueue.
            LPVSQueue webhookConfig = queueService.getQueueFirstElement();
            log.info("PROCESS Webhook id = " + webhookConfig.getId());

            // Set the date of the LPVSQueue element.
            webhookConfig.setDate(new Date());

            // Process the LPVSQueue element.
            queueService.processWebHook(webhookConfig);
        }
    }
}
