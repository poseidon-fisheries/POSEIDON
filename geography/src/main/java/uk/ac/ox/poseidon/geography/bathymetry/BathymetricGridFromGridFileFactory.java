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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.poseidon.geography.bathymetry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.geotools.api.referencing.operation.MathTransform2D;
import org.geotools.api.referencing.operation.TransformException;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.aggregators.Aggregator;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.CoverageWrapper;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.awt.geom.Point2D;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

@NoArgsConstructor
public class BathymetricGridFromGridFileFactory extends BathymetricGridFactory {

    public BathymetricGridFromGridFileFactory(
        @NonNull final Factory<? extends Path> path,
        @NonNull final Factory<? extends ModelGrid> modelGrid,
        @NonNull final Factory<? extends Aggregator> aggregator,
        final boolean inverted
    ) {
        super(path, modelGrid, aggregator, inverted);
    }

    @Override
    protected Map<Int2D, Collection<Double>> readElevationValues(
        final File gridFile,
        final ModelGrid modelGrid
    ) {
        final CoverageWrapper coverageWrapper = new CoverageWrapper(gridFile);
        final Multimap<Int2D, Double> elevationValues = ArrayListMultimap.create();
        final MathTransform2D gridToCRS2D =
            coverageWrapper.getCoverage().getGridGeometry().getGridToCRS2D();

        // worldPos will be mutated while reading
        final Point2D.Double worldPos = new Point2D.Double();
        coverageWrapper.processGrid((int2D, value) -> {
            try {
                gridToCRS2D.transform(new Point2D.Double(int2D.x, int2D.y), worldPos);
                final Int2D cell = modelGrid.toCell(Coordinate.fromPoint2D(worldPos));
                final double elevation = isInverted() ? -value : value;
                elevationValues.put(cell, elevation);
            } catch (final TransformException e) {
                throw new RuntimeException(e);
            }
        });
        return elevationValues.asMap();
    }

}
