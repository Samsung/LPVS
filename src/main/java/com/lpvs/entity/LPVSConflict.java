/**
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * Represents a license conflict between two licenses.
 *
 * @param <License1> Type of the first license.
 * @param <License2> Type of the second license.
 */
@Getter
@AllArgsConstructor
public class LPVSConflict<License1, License2> {

    /**
     * The first license in the conflict.
     */
    private final License1 l1;

    /**
     * The second license in the conflict.
     */
    private final License2 l2;

    /**
     * Compares this LPVSConflict object with another object for equality.
     *
     * @param o The object to compare with this LPVSConflict.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LPVSConflict<?, ?> conflict = (LPVSConflict<?, ?>) o;
        return (l1.equals(conflict.l1) && l2.equals(conflict.l2))
                || (l1.equals(conflict.l2) && l2.equals(conflict.l1));
    }

    /**
     * Generates a hash code value for this LPVSConflict object.
     * The hash code is computed based on the hash codes of the two licenses.
     *
     * @return A hash code value for this LPVSConflict object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(l1, l2);
    }
}
