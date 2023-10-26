/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.exception;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {

    private LocalDateTime timestamp = LocalDateTime.now();
    private String message;
    private String code;
    private int status;

    public ErrorResponse(String message, String code, int status) {
        this.message = message;
        this.code = code;
        this.status = status;
    }
}
