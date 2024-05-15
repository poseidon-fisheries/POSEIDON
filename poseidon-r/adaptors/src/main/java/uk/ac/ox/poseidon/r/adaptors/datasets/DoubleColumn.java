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

public class DoubleColumn extends RColumn<Double> {

    protected static final double NA_REAL = Double.longBitsToDouble(0x7ff00000000007a2L);
    private final double[] doubles;

    public DoubleColumn(
        final String name,
        final Iterable<?> objects
    ) {
        this(name, Streams.stream(objects));
    }

    public DoubleColumn(
        final String name,
        final Stream<?> objects
    ) {
        this(
            name,
            objects
                .mapToDouble(o -> o instanceof Number ? ((Number) o).doubleValue() : NA_REAL)
                .toArray()
        );
    }

    public DoubleColumn(
        final String name,
        final double[] doubles
    ) {
        super(name);
        this.doubles = doubles;
    }

    @Override
    public Object toArray() {
        return doubles;
    }

    @Override
    public Iterator<Double> iterator() {
        return Arrays.stream(doubles).iterator();
    }
}
