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
import org.springframework.web.bind.annotation.GetMapping;

import com.lpvs.util.LPVSExitHandler;

@SpringBootApplication(scanBasePackages = {"com.lpvs"})
@EnableAutoConfiguration
@EnableAsync
public class LicensePreValidationSystem {

    private final int corePoolSize;

    private static LPVSExitHandler exitHandler;

    public LicensePreValidationSystem(@Value("${lpvs.cores:8}") int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

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

    @Bean("threadPoolTaskExecutor")
    public TaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setThreadNamePrefix("LPVS-ASYNC::");
        return executor;
    }

    @GetMapping("/exit")
    public static void exit(int exitCode) {
        exitHandler.exit(exitCode);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
