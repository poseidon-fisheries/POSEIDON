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

import org.junit.jupiter.api.Test;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.geography.Coordinate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElevationTableTest {

    @Test
    void entryStreamReturnsCoordinatesAndElevationsInRowOrder() {
        final Table table = Table.create("elevation-data")
            .addColumns(
                DoubleColumn.create("lon", 10.0, 20.0, 30.0),
                DoubleColumn.create("lat", -1.0, -2.0, -3.0),
                DoubleColumn.create("elev", 100.0, 200.0, 300.0)
            );

        final ElevationTable elevationTable = new ElevationTable(
            table,
            "lon",
            "lat",
            "elev"
        );

        final List<ElevationTable.Entry> entries = elevationTable.entryStream().toList();

        assertThat(entries).hasSize(3);
        assertThat(entries.get(0).getCoordinate()).isEqualTo(new Coordinate(10.0, -1.0));
        assertThat(entries.get(0).getElevation()).isEqualTo(100.0);
        assertThat(entries.get(1).getCoordinate()).isEqualTo(new Coordinate(20.0, -2.0));
        assertThat(entries.get(1).getElevation()).isEqualTo(200.0);
        assertThat(entries.get(2).getCoordinate()).isEqualTo(new Coordinate(30.0, -3.0));
        assertThat(entries.get(2).getElevation()).isEqualTo(300.0);
    }
}
