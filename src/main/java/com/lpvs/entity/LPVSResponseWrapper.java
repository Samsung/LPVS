/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity;

/**
 * Represents a response wrapper in the LPVS system.
 * This class is used to wrap messages in responses.
 */
public class LPVSResponseWrapper {

    /**
     * The message included in the response wrapper.
     */
    private String message;

    /**
     * Constructs a new response wrapper with the given message.
     *
     * @param message The message to be included in the response wrapper.
     */
    public LPVSResponseWrapper(String message) {
        this.message = message;
    }

    /**
     * Retrieves the message from the response wrapper.
     *
     * @return The message included in the response wrapper.
     */
    public String getMessage() {
        return this.message;
    }
}
