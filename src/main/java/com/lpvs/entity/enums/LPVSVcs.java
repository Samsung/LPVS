/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.enums;

/**
 * Represents the version control systems (VCS) supported by the LPVS system.
 * Each VCS corresponds to a specific type of version control platform.
 */
public enum LPVSVcs {

    /**
     * Represents the GitHub version control system.
     */
    GITHUB("github"),

    /**
     * Represents the Swarm version control system.
     */
    SWARM("swarm"),

    /**
     * Represents the Gerrit version control system.
     */
    GERRIT("gerrit"),

    /**
     * Represents the GitLab version control system.
     */
    GITLAB("gitlab");

    /**
     * The string representation of the version control system.
     */
    private final String vcs;

    /**
     * Constructs an LPVSVcs with the specified VCS type.
     *
     * @param vcs The string representation of the version control system.
     */
    LPVSVcs(final String vcs) {
        this.vcs = vcs;
    }

    /**
     * Gets the string representation of the version control system.
     *
     * @return The string representation of the version control system.
     */
    public String getVcs() {
        return vcs;
    }

    /**
     * Returns a string representation of the version control system.
     *
     * @return The string representation of the version control system.
     */
    @Override
    public String toString() {
        return getVcs();
    }

    /**
     * Converts a string representation of a version control system to the corresponding enum constant.
     *
     * @param action The string representation of the version control system.
     * @return The corresponding LPVSVcs enum constant, or null if not found.
     */
    public static LPVSVcs convertFrom(String action) {
        if (action.equals(GITHUB.getVcs())) {
            return GITHUB;
        } else if (action.equals(GERRIT.getVcs())) {
            return GERRIT;
        } else if (action.equals(SWARM.getVcs())) {
            return SWARM;
        } else if (action.equals(GITLAB.getVcs())) {
            return GITLAB;
        } else {
            return null;
        }
    }
}
