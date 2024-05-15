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
package uk.ac.ox.poseidon.r.adaptors.datasets.timeseries;

import uk.ac.ox.poseidon.datasets.api.Column;
import uk.ac.ox.poseidon.r.adaptors.datasets.IntegerColumn;

import static com.google.common.collect.Streams.mapWithIndex;

class YearlyIndexColumn extends IntegerColumn {

    YearlyIndexColumn(
        final String name,
        final Column<?> indexedColumn,
        final long startYear
    ) {
        super(
            name,
            mapWithIndex(indexedColumn.stream(), (__, index) -> startYear + index)
                .mapToInt(Math::toIntExact)
                .toArray()
        );
    }
}
