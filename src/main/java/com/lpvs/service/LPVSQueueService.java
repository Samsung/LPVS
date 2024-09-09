/**
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service;

import com.lpvs.entity.LPVSQueue;
import com.lpvs.repository.LPVSQueueRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Service for managing LPVSQueue elements and processing webhooks.
 */
@Service
@Slf4j
public class LPVSQueueService {

    /**
     * Repository for storing LPVSQueue entities.
     */
    private final LPVSQueueRepository queueRepository;

    /**
     * BlockingDeque for managing LPVSQueue elements.
     */
    private static final BlockingDeque<LPVSQueue> QUEUE = new LinkedBlockingDeque<>();

    /**
     * Constructor for LPVSQueueService.
     *
     * @param queueRepository Repository for storing LPVSQueue entities.
     */
    @Autowired
    public LPVSQueueService(LPVSQueueRepository queueRepository) {
        this.queueRepository = queueRepository;
    }

    /**
     * Gets the first element from the LPVSQueue.
     *
     * @return The first LPVSQueue element.
     * @throws InterruptedException If interrupted while waiting for the element.
     */
    public LPVSQueue getQueueFirstElement() throws InterruptedException {
        return QUEUE.takeFirst();
    }

    /**
     * Adds the LPVSQueue element to the front of the queue.
     *
     * @param webhookConfig The LPVSQueue element to be added.
     * @throws InterruptedException If interrupted while waiting to add the element.
     */
    public void addFirst(LPVSQueue webhookConfig) throws InterruptedException {
        QUEUE.putFirst(webhookConfig);
    }

    /**
     * Adds the LPVSQueue element to the end of the queue.
     *
     * @param webhookConfig The LPVSQueue element to be added.
     * @throws InterruptedException If interrupted while waiting to add the element.
     */
    public void add(LPVSQueue webhookConfig) throws InterruptedException {
        QUEUE.put(webhookConfig);
    }

    /**
     * Deletes the LPVSQueue element from the repository and the queue.
     *
     * @param webhookConfig The LPVSQueue element to be deleted.
     */
    public void delete(LPVSQueue webhookConfig) {
        queueRepository.deleteById(webhookConfig.getId());
        QUEUE.remove(webhookConfig);
    }

    /**
     * Gets the entire LPVSQueue.
     *
     * @return The BlockingDeque containing LPVSQueue elements.
     */
    public BlockingDeque<LPVSQueue> getQueue() {
        return QUEUE;
    }

    /**
     * Checks for any previous LPVSQueue elements and processes them.
     *
     * @throws InterruptedException If interrupted while processing the queue.
     */
    public void checkForQueue() throws InterruptedException {
        QUEUE.clear();
        log.debug("Checking for previous queue");
        List<LPVSQueue> webhookConfigList = queueRepository.findAll();
        for (LPVSQueue webhook : webhookConfigList) {
            log.info("Add WebHook id = " + webhook.getId() + " to the queue.");
            QUEUE.putFirst(webhook);
        }
    }
}
