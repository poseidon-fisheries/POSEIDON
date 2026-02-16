/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class Factories {

    private Factories() {
    }

    public static <T> ObjectFactory<T> object(final T value) {
        return new ObjectFactory<>(value);
    }

    @SafeVarargs
    public static <T> ObjectFactory<List<T>> listOf(final T... values) {
        return new ObjectFactory<>(ImmutableList.copyOf(values));
    }

    public static <T> ObjectFactory<List<T>> listOf(final Stream<T> values) {
        return new ObjectFactory<>(values.collect(toImmutableList()));
    }

    @SafeVarargs
    public static <T> ObjectFactory<Set<T>> setOf(final T... values) {
        return new ObjectFactory<>(ImmutableSet.copyOf(values));
    }

    public static <T> ObjectFactory<Set<T>> setOf(final Stream<T> values) {
        return new ObjectFactory<>(values.collect(toImmutableSet()));
    }

}
