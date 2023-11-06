/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class LPVSExitHandler {

    private ApplicationContext applicationContext;

    @Autowired
    public LPVSExitHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public int exit(int exitCode) {
        return SpringApplication.exit(applicationContext, () -> exitCode);
    }
}
