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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import tech.tablesaw.api.NumericColumn;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.geography.Coordinate;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Wraps a {@code tablesaw} {@link Table} that has longitude and latitude columns, exposing its
 * rows as a stream of {@link Coordinate}s. Built via
 * {@link Factories#lonLatTable(uk.ac.ox.poseidon.core.Factory, String, String)} in this package;
 * subclassed by {@link ElevationTable} for tables with a further elevation column.
 */
public class LonLatTable {
    /** The wrapped table. */
    protected final Table table;
    /** The table's longitude column, resolved by name at construction time. */
    protected final NumericColumn<?> longitudeColumn;
    /** The table's latitude column, resolved by name at construction time. */
    protected final NumericColumn<?> latitudeColumn;

    /**
     * @param table               the table to wrap
     * @param longitudeColumnName the name of the column holding longitude values
     * @param latitudeColumnName  the name of the column holding latitude values
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public LonLatTable(
        final Table table,
        final String longitudeColumnName,
        final String latitudeColumnName
    ) {
        this.table = table;
        this.longitudeColumn = table.numberColumn(longitudeColumnName);
        this.latitudeColumn = table.numberColumn(latitudeColumnName);
    }

    /** @return one {@link Coordinate} per row, in table order */
    public Stream<Coordinate> coordinateStream() {
        return IntStream.range(0, table.rowCount())
            .mapToObj(i ->
                new Coordinate(
                    longitudeColumn.getDouble(i),
                    latitudeColumn.getDouble(i)
                )
            );
    }

}
