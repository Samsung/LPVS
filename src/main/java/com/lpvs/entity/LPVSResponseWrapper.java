/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity;

public class LPVSResponseWrapper {
    private String message;

    public LPVSResponseWrapper(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
