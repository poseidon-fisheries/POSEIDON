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

package uk.ac.ox.poseidon.geography.grids;

import com.google.common.collect.ImmutableSet;
import lombok.*;
import org.geotools.api.geometry.Bounds;
import org.geotools.coverage.grid.GridCoverage2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.Envelope;

import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.nio.file.Path;

import static uk.ac.ox.poseidon.geography.Utils.readCoverage;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModelGridFromEsriAsciiExclusionGridFactory
    extends GlobalScopeFactory<ModelGrid> {
    @NonNull private Factory<? extends Path> path;
    private int excludedValue;

    private static Envelope makeEnvelope(final GridCoverage2D coverage) {
        final Bounds bounds = coverage.getEnvelope();
        return new Envelope(
            bounds.getMinimum(0),
            bounds.getMaximum(0),
            bounds.getMinimum(1),
            bounds.getMaximum(1)
        );
    }

    @Override
    protected ModelGrid newInstance(final Simulation simulation) {
        final GridCoverage2D coverage = readCoverage(path.get(simulation).toFile());
        final RenderedImage image = coverage.getRenderedImage();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final Raster raster = image.getData();
        final ImmutableSet.Builder<Int2D> inactiveCells = ImmutableSet.builder();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (raster.getSampleDouble(x, y, 0) == excludedValue) {
                    inactiveCells.add(new Int2D(x, y));
                }
            }
        }
        return ModelGrid.withInactiveCells(
            width,
            height,
            makeEnvelope(coverage),
            inactiveCells.build()
        );
    }

}
