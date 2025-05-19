/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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
package uk.ac.ox.poseidon.r.adaptors.datasets;

import com.google.common.collect.Streams;

import java.time.LocalDate;
import java.util.stream.Stream;

public class DateColumn extends DoubleColumn {

    private static final String[] S3_CLASSES = new String[]{"Date"};

    DateColumn(
        final String name,
        final Iterable<Object> objects
    ) {
        this(
            name,
            Streams.stream(objects)
        );
    }

    public DateColumn(
        final String name,
        final Stream<?> objects
    ) {
        this(
            name,
            objects
                .mapToDouble(o ->
                    o instanceof LocalDate
                        ? (double) ((LocalDate) o).toEpochDay()
                        : NA_REAL
                )
                .toArray()
        );
    }

    private DateColumn(
        final String name,
        final double[] doubles
    ) {
        super(name, doubles);
    }

    @Override
    public String[] getS3Classes() {
        return S3_CLASSES;
    }
}
