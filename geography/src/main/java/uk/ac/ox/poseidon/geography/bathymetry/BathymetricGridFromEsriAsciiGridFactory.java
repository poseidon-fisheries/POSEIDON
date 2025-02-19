/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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

import lombok.*;
import org.apache.sis.coverage.grid.GridCoverage;
import org.apache.sis.image.PixelIterator;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.DataStores;
import org.apache.sis.storage.GridCoverageResource;
import sim.field.grid.DoubleGrid2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.awt.*;
import java.awt.image.RenderedImage;
import java.nio.file.Path;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BathymetricGridFromEsriAsciiGridFactory
    extends GlobalScopeFactory<BathymetricGrid> {

    @NonNull private Factory<? extends Path> path;
    @NonNull private Factory<? extends ModelGrid> modelGrid;
    private boolean inverted = false;

    @Override
    protected BathymetricGrid newInstance(final Simulation simulation) {

        final ModelGrid modelGrid = this.modelGrid.get(simulation);

        try (final DataStore store = DataStores.open(this.path.get(simulation))) {
            // Assuming that we know that the data is a single raster:
            final GridCoverageResource r = (GridCoverageResource) store;

            // Subset of data could be specified here (no subset in this example):
            final GridCoverage coverage = r.read(null, null);

            final DoubleGrid2D doubleGrid2D = new DoubleGrid2D(modelGrid.makeDoubleArray());

            final RenderedImage image = coverage.render(coverage.getGridGeometry().getExtent());
            final PixelIterator pit = PixelIterator.create(image);
            while (pit.next()) {
                final Point pos = pit.getPosition();
                if (modelGrid.isInGrid(new Int2D(pos))) {
                    final double value = pit.getSampleDouble(0);
                    doubleGrid2D.set(pos.x, pos.y, inverted ? -value : value);
                }
            }

            return new DefaultBathymetricGrid(
                modelGrid,
                doubleGrid2D
            );

        } catch (final DataStoreException e) {
            throw new RuntimeException(e);
        }
    }
}
