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
 *
 */

package uk.ac.ox.poseidon.geography.bathymetry;

import com.vividsolutions.jts.geom.Coordinate;
import lombok.*;
import sim.field.grid.DoubleGrid2D;
import sim.util.Int2D;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.grids.GridExtent;

import java.nio.file.Path;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BathymetricGridFromLongFormatCsvFactory
    extends GlobalScopeFactory<BathymetricGrid> {

    @NonNull private Factory<? extends Path> path;
    @NonNull private Factory<? extends GridExtent> gridExtent;
    @NonNull private String longitudeColumnName;
    @NonNull private String latitudeColumnName;
    @NonNull private String depthColumnName;
    private double defaultDepth;

    @Override
    protected BathymetricGrid newInstance(final Simulation simulation) {
        final GridExtent gridExtent = this.gridExtent.get(simulation);
        final DoubleGrid2D doubleGrid2D =
            new DoubleGrid2D(
                gridExtent.getGridWidth(),
                gridExtent.getGridHeight(),
                defaultDepth
            );
        Table.read().file(path.get(simulation).toFile()).forEach(row -> {
            final Int2D cell =
                gridExtent.toCell(new Coordinate(
                    row.getDouble(longitudeColumnName),
                    row.getDouble(latitudeColumnName)
                ));
            doubleGrid2D.set(
                cell.x,
                cell.y,
                row.getDouble(depthColumnName)
            );
        });
        return new DefaultBathymetricGrid(gridExtent, doubleGrid2D);
    }

}
