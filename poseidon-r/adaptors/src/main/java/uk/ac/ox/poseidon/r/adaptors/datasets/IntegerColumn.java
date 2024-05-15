/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
package uk.ac.ox.poseidon.r.adaptors.datasets;

import com.google.common.collect.Streams;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

public class IntegerColumn extends RColumn<Integer> {

    private static final int NA_INTEGER = -2147483648;

    private final int[] ints;

    public IntegerColumn(
        final String name,
        final Iterable<?> objects
    ) {
        this(name, Streams.stream(objects));
    }

    public IntegerColumn(
        final String name,
        final Stream<?> objects
    ) {
        this(
            name,
            objects
                .mapToInt(o -> o instanceof Number ? ((Number) o).intValue() : NA_INTEGER)
                .toArray()
        );
    }

    public IntegerColumn(
        final String name,
        final int[] ints
    ) {
        super(name);
        this.ints = ints;
    }

    @Override
    public Object toArray() {
        return ints;
    }

    @Override
    public Iterator<Integer> iterator() {
        return Arrays.stream(ints).iterator();
    }
}
