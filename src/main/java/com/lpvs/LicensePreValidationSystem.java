/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.lpvs.util.LPVSExitHandler;

/**
 * The main class for the License Pre-Validation Service (LPVS) application.
 * This class configures and launches the LPVS Spring Boot application.
 */
@SpringBootApplication(scanBasePackages = {"com.lpvs"})
@EnableAutoConfiguration
@EnableAsync
public class LicensePreValidationSystem {

    /**
     * The core pool size for the asynchronous task executor.
     */
    private final int corePoolSize;

    /**
     * The exit handler for handling application exits.
     */
    private static LPVSExitHandler exitHandler;

    /**
     * Constructs a new LicensePreValidationSystem with the specified core pool size.
     *
     * @param corePoolSize The core pool size for the asynchronous task executor.
     */
    public LicensePreValidationSystem(@Value("${lpvs.cores:8}") int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    /**
     * The main entry point of the LPVS application.
     *
     * @param args The command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        try {
            ApplicationContext applicationContext =
                    SpringApplication.run(LicensePreValidationSystem.class, args);
            exitHandler = applicationContext.getBean(LPVSExitHandler.class);
        } catch (IllegalArgumentException e) {
            System.err.println("An IllegalArgumentException occurred: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Configures and retrieves an asynchronous task executor bean.
     *
     * @return An asynchronous task executor bean.
     */
    @Bean("threadPoolTaskExecutor")
    public TaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setThreadNamePrefix("LPVS-ASYNC::");
        return executor;
    }
}
