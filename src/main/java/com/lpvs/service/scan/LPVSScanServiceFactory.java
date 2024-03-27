/**
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service.scan;

import com.lpvs.repository.LPVSLicenseRepository;
import com.lpvs.service.LPVSLicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;

/**
 * Factory class for creating instances of {@link LPVSScanService}.
 */
@Service
public class LPVSScanServiceFactory {

    /**
     * Flag indicating whether the application is in debug mode.
     */
    @Value("${debug:false}")
    Boolean debug;

    /**
     * The service for managing licenses, providing operations related to licenses.
     */
    @Autowired LPVSLicenseService licenseService;

    /**
     * The repository for LPVSLicense entities, allowing database interactions for licenses.
     */
    @Autowired LPVSLicenseRepository lpvsLicenseRepository;

    /**
     * Creates a scan service based on the specified scanner type and configuration.
     *
     * @param scannerType The type of scanner to create.
     * @param isInternal  Flag indicating whether the scanner is internal or not.
     * @return An instance of {@link LPVSScanService} corresponding to the specified scanner type.
     * @throws IllegalArgumentException if the specified scanner type is not supported or if an error occurs during
     *                                  the creation process.
     */
    public LPVSScanService createScanService(String scannerType, boolean isInternal) {
        try {
            Class<?> serviceClass = Class.forName(getServiceClassName(scannerType, isInternal));
            Constructor<?> constructor =
                    serviceClass.getDeclaredConstructor(
                            Boolean.class, LPVSLicenseService.class, LPVSLicenseRepository.class);
            return (LPVSScanService)
                    constructor.newInstance(debug, licenseService, lpvsLicenseRepository);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Error creating scan service for type: " + scannerType, e);
        }
    }

    /**
     * Gets the fully qualified class name of the scan service based on the specified scanner type and configuration.
     *
     * @param scannerType The type of scanner.
     * @param isInternal  Flag indicating whether the scanner is internal or not.
     * @return The fully qualified class name of the scan service.
     * @throws IllegalArgumentException if the specified scanner type is null or empty string.
     */
    protected String getServiceClassName(String scannerType, boolean isInternal) {
        if (scannerType != null && !scannerType.isEmpty()) {
            return "com.lpvs."
                    + (isInternal ? "internal." : "")
                    + "service.scan.scanner.LPVS"
                    + scannerType.substring(0, 1).toUpperCase()
                    + scannerType.substring(1)
                    + "DetectService";
        } else {
            throw new IllegalArgumentException("Scanner type cannot be null or empty.");
        }
    }
}
