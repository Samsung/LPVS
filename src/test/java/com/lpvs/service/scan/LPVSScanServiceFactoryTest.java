/**
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service.scan;

import com.lpvs.service.scan.scanner.LPVSScanossDetectService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LPVSScanServiceFactoryTest {

    private LPVSScanServiceFactory scanServiceFactory = new LPVSScanServiceFactory();

    @Test
    void testGetServiceClassName() {
        String expected1 = "com.lpvs.internal.service.scan.scanner.LPVSFooDetectService";
        String actual1 = scanServiceFactory.getServiceClassName("foo", true);
        assertEquals(expected1, actual1);

        String expected2 = "com.lpvs.service.scan.scanner.LPVSFooDetectService";
        String actual2 = scanServiceFactory.getServiceClassName("foo", false);
        assertEquals(expected2, actual2);
    }

    @Test
    void testGetServiceClassName_NullScannerType_N() {
        assertThrows(
                IllegalArgumentException.class,
                () -> scanServiceFactory.getServiceClassName(null, true));
    }

    @Test
    void testGetServiceClassName_EmptyScannerType_N() {
        assertThrows(
                IllegalArgumentException.class,
                () -> scanServiceFactory.getServiceClassName("", true));
    }

    @Test
    void testCreateScanService() {
        LPVSScanService service = scanServiceFactory.createScanService("scanoss", false);
        assertNotNull(service);
        assertTrue(service instanceof LPVSScanossDetectService);
    }

    @Test
    void testCreateScanService_NullScannerType_N() {
        assertThrows(
                IllegalArgumentException.class,
                () -> scanServiceFactory.createScanService(null, true));
    }

    @Test
    void testCreateScanService_EmptyScannerType_N() {
        assertThrows(
                IllegalArgumentException.class,
                () -> scanServiceFactory.createScanService("", true));
    }

    @Test
    void testCreateScanService_NoSuchScannerType_N() {
        assertThrows(
                IllegalArgumentException.class,
                () -> scanServiceFactory.createScanService("baz", true));
    }
}
