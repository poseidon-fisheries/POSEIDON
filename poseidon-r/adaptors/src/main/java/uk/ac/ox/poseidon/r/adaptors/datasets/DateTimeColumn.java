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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;

public class DateTimeColumn extends DoubleColumn {

    private static final String[] S3_CLASSES = new String[]{"POSIXct", "POSIXt"};

    DateTimeColumn(
        final String name,
        final Iterable<?> objects
    ) {
        this(name, Streams.stream(objects));
    }

    private DateTimeColumn(
        final String name,
        final Stream<?> objects
    ) {
        this(
            name,
            objects
                .mapToDouble(o ->
                    o instanceof LocalDateTime
                        ? (double) ((LocalDateTime) o).toEpochSecond(ZoneOffset.UTC)
                        : NA_REAL
                )
                .toArray()
        );
    }

    private DateTimeColumn(
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
