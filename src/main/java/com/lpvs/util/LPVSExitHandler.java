/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Component class providing an exit handler for the LPVS application.
 * This class is responsible for gracefully shutting down the Spring application context
 * and terminating the application with the specified exit code.
 */
@Component
public class LPVSExitHandler {

    /**
     * The Spring application context used for handling the application exit.
     */
    private ApplicationContext applicationContext;

    /**
     * Constructs an instance of {@code LPVSExitHandler} with the provided application context.
     *
     * @param applicationContext The Spring application context.
     */
    @Autowired
    public LPVSExitHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Initiates the application exit process with the specified exit code.
     *
     * @param exitCode The exit code to be used when terminating the application.
     * @return The exit code that was passed as a parameter.
     */
    public int exit(int exitCode) {
        return SpringApplication.exit(applicationContext, () -> exitCode);
    }
}
