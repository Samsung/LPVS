/**
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service.webhook;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LPVSWebhookServiceFactoryTest {

    private LPVSWebhookServiceFactory webhookServiceFactory = new LPVSWebhookServiceFactory();

    @Test
    void testCreateScanService() {
        LPVSWebhookService service = webhookServiceFactory.createWebhookService(false);
        assertNotNull(service);
        assertTrue(service instanceof LPVSWebhookService);
    }

    @Test
    void testCreateScanService_NoSuchWebhookService_N() {
        assertThrows(
                IllegalArgumentException.class,
                () -> webhookServiceFactory.createWebhookService(true));
    }
}
