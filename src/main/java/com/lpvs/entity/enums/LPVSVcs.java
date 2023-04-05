/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity.enums;

public enum LPVSVcs {
    GITHUB("github"),
    SWARM("swarm"),
    GERRIT("gerrit"),
    GITLAB("gitlab");

    private final String vcs;

    LPVSVcs(final String vcs) {
        this.vcs = vcs;
    }

    public String getVcs() {
        return vcs;
    }

    @Override
    public String toString() {
        return getVcs();
    }

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
