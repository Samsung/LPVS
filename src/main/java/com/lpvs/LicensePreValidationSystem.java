/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@EnableAsync
public class LicensePreValidationSystem {

    private int corePoolSize;

    private String lpvsVersion;

    private static Logger LOG = LoggerFactory.getLogger(LicensePreValidationSystem.class);

    public LicensePreValidationSystem(@Value("${lpvs.cores:8}") int corePoolSize,
                                      @Value("${lpvs.version:}") String lpvsVersion) {
        this.corePoolSize = corePoolSize;
        this.lpvsVersion = lpvsVersion;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(LicensePreValidationSystem.class);


        app.run(args);
    }

    @Bean("threadPoolTaskExecutor")
    public TaskExecutor getAsyncExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        LOG.info("License Pre-Validation Service (LPVS) version " + lpvsVersion + " is running.");
        executor.setCorePoolSize(corePoolSize);
        LOG.info("Used core pool size is " + corePoolSize);
        executor.setThreadNamePrefix("LPVS-ASYNC::");
        return executor;
    }

}

