/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.poseidon.core.utils;

import lombok.Value;

/**
 * An immutable pair of two values, of possibly different types. Built via
 * {@link Factories#pair(uk.ac.ox.poseidon.core.Factory, uk.ac.ox.poseidon.core.Factory)} in this
 * package.
 *
 * @param <A> the type of the first value
 * @param <B> the type of the second value
 */
@Value
public class Pair<A, B> {

    A first;
    B second;

    /**
     * @param a the first value
     * @param b the second value
     * @return a pair of the two values
     */
    public static <A, B> Pair<A, B> of(
        final A a,
        final B b
    ) {
        return new Pair<>(a, b);
    }

}
