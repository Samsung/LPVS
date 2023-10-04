/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.exception;

public class WrongAccessException extends RuntimeException {

    public WrongAccessException(String message) {
        super(message);
    }
}
