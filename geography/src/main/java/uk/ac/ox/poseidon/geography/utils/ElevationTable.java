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

package uk.ac.ox.poseidon.geography.utils;

import lombok.Getter;
import lombok.Value;
import tech.tablesaw.api.NumericColumn;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.geography.Coordinate;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A {@link LonLatTable} that also has an elevation column, exposing its rows as a stream of
 * coordinate/elevation {@link Entry} pairs. Built via
 * {@link Factories#elevationTable(uk.ac.ox.poseidon.core.Factory, String, String, String)} in
 * this package.
 */
@Getter
public class ElevationTable extends LonLatTable {

    private final NumericColumn<?> elevationColumn;

    /**
     * @param table               the table to wrap
     * @param longitudeColumnName the name of the column holding longitude values
     * @param latitudeColumnName  the name of the column holding latitude values
     * @param elevationColumnName the name of the column holding elevation values
     */
    public ElevationTable(
        final Table table,
        final String longitudeColumnName,
        final String latitudeColumnName,
        final String elevationColumnName
    ) {
        super(table, longitudeColumnName, latitudeColumnName);
        this.elevationColumn = table.numberColumn(elevationColumnName);
    }

    /** One row's coordinate and elevation. */
    @Value
    public static class Entry {
        Coordinate coordinate;
        double elevation;
    }

    /** @return one {@link Entry} per row, in table order */
    public Stream<Entry> entryStream() {
        return IntStream.range(0, table.rowCount())
            .mapToObj(i -> new Entry(
                new Coordinate(
                    longitudeColumn.getDouble(i),
                    latitudeColumn.getDouble(i)
                ),
                elevationColumn.getDouble(i)
            ));
    }

}
