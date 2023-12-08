/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.enums;

/**
 * Represents the actions that can be performed on a pull request in the LPVS system.
 * Each action corresponds to a specific event in the lifecycle of a pull request.
 */
public enum LPVSPullRequestAction {

    /**
     * Represents the action of opening a pull request.
     */
    OPEN("opened"),

    /**
     * Represents the action of reopening a closed pull request.
     */
    REOPEN("reopened"),

    /**
     * Represents the action of closing a pull request.
     */
    CLOSE("closed"),

    /**
     * Represents the action of updating a pull request.
     */
    UPDATE("synchronize"),

    /**
     * Represents the action of triggering a rescan of a pull request.
     */
    RESCAN("rescan");

    /**
     * The string representation of the pull request action.
     */
    private final String type;

    /**
     * Constructs an LPVSPullRequestAction with the specified type.
     *
     * @param type The string representation of the pull request action.
     */
    LPVSPullRequestAction(final String type) {
        this.type = type;
    }

    /**
     * Gets the string representation of the pull request action.
     *
     * @return The string representation of the pull request action.
     */
    public String getPullRequestAction() {
        return type;
    }

    /**
     * Converts a string representation of a pull request action to the corresponding enum constant.
     *
     * @param action The string representation of the pull request action.
     * @return The corresponding LPVSPullRequestAction enum constant, or null if not found.
     */
    public static LPVSPullRequestAction convertFrom(String action) {
        if (action.equals(OPEN.getPullRequestAction())) {
            return OPEN;
        } else if (action.equals(REOPEN.getPullRequestAction())) {
            return REOPEN;
        } else if (action.equals(CLOSE.getPullRequestAction())) {
            return CLOSE;
        } else if (action.equals(UPDATE.getPullRequestAction())) {
            return UPDATE;
        } else if (action.equals(RESCAN.getPullRequestAction())) {
            return RESCAN;
        } else {
            return null;
        }
    }
}
