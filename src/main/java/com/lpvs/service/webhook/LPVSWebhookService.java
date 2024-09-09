/**
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service.webhook;

import com.lpvs.entity.LPVSQueue;

/**
 * The LPVSWebhookService interface defines the contract for processing LPVSQueue elements and handling webhook events.
 */
public interface LPVSWebhookService {

    /**
     * Processes the LPVSQueue element, handling webhook events.
     *
     * @param webhookConfig The LPVSQueue element to be processed.
     */
    void processWebHook(LPVSQueue webhookConfig);
}
