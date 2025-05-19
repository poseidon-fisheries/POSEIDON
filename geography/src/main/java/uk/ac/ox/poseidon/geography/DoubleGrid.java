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

package uk.ac.ox.poseidon.geography;

import com.univocity.parsers.common.record.Record;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;

import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

public class DoubleGrid {
    private final MapExtent mapExtent;
    private final DoubleGrid2D gridField;

    private DoubleGrid(
        final MapExtent mapExtent,
        final DoubleGrid2D gridField
    ) {
        checkArgument(gridField.getWidth() == mapExtent.getGridWidth());
        checkArgument(gridField.getHeight() == mapExtent.getGridHeight());
        this.mapExtent = mapExtent;
        this.gridField = gridField;
    }

    public static DoubleGrid from(
        final MapExtent mapExtent
    ) {
        return new DoubleGrid(
            mapExtent,
            new DoubleGrid2D(
                mapExtent.getGridWidth(),
                mapExtent.getGridHeight()
            )
        );
    }

    public static DoubleGrid from(
        final MapExtent mapExtent,
        final double initialValue
    ) {
        return new DoubleGrid(
            mapExtent,
            new DoubleGrid2D(
                mapExtent.getGridWidth(),
                mapExtent.getGridHeight(),
                initialValue
            )
        );
    }

    public static DoubleGrid from(
        final MapExtent mapExtent,
        final double[][] values
    ) {
        return new DoubleGrid(mapExtent, new DoubleGrid2D(values));
    }

    public static DoubleGrid fromRecords(
        final MapExtent mapExtent,
        final Stream<? extends Record> records,
        final String longitudeColumnName,
        final String latitudeColumnName,
        final String valueColumnName
    ) {
        final DoubleGrid2D grid = new DoubleGrid2D(
            mapExtent.getGridWidth(),
            mapExtent.getGridHeight()
        );
        records.forEach(record -> {
            final double lon = record.getDouble(longitudeColumnName);
            final double lat = record.getDouble(latitudeColumnName);
            final int x = mapExtent.toGridX(lon);
            final int y = mapExtent.toGridY(lat);
            if (x < grid.getWidth() && y < grid.getHeight() && x >= 0 && y >= 0) {
                grid.set(x, y, record.getDouble(valueColumnName));
            }
        });
        return DoubleGrid.from(mapExtent, grid);
    }

    public static DoubleGrid from(
        final MapExtent mapExtent,
        final DoubleGrid2D values
    ) {
        return new DoubleGrid(mapExtent, new DoubleGrid2D(values));
    }

    public MapExtent getMapExtent() {
        return mapExtent;
    }

    public final double get(
        final int x,
        final int y
    ) {
        return gridField.get(x, y);
    }

}
