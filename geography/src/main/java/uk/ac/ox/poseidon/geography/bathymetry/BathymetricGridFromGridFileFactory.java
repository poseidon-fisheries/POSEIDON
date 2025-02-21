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
import lombok.NonNull;
import org.geotools.api.referencing.operation.MathTransform2D;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.coverage.grid.GridCoverage2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.aggregators.Aggregator;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.awt.geom.Point2D;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import static uk.ac.ox.poseidon.geography.Utils.readCoverage;

public class BathymetricGridFromGridFileFactory extends BathymetricGridFactory {

    public BathymetricGridFromGridFileFactory(
        @NonNull final Factory<? extends Path> path,
        @NonNull final Factory<? extends ModelGrid> modelGrid,
        @NonNull final Factory<? extends Aggregator> aggregator,
        final boolean inverted
    ) {
        super(path, modelGrid, aggregator, inverted);
    }

    public BathymetricGridFromGridFileFactory() {
    }

    @Override
    protected Map<Int2D, Collection<Double>> readElevationValues(
        final File gridFile,
        final ModelGrid modelGrid
    ) {
        final GridCoverage2D coverage = readCoverage(gridFile);
        final Multimap<Int2D, Double> elevationValues = ArrayListMultimap.create();
        final MathTransform2D gridToCRS2D = coverage.getGridGeometry().getGridToCRS2D();
        final int width = coverage.getRenderedImage().getWidth();
        final int height = coverage.getRenderedImage().getHeight();

        // elevation and worldPos will both be mutated while reading
        final Point2D.Double worldPos = new Point2D.Double();
        final double[] elevation = new double[1];
        try {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    gridToCRS2D.transform(new Point2D.Double(x, y), worldPos);
                    final Int2D cell = modelGrid.toCell(new Coordinate(worldPos.x, worldPos.y));
                    final double value = coverage.evaluate(worldPos, elevation)[0];
                    elevationValues.put(cell, isInverted() ? -value : value);
                }
            }
        } catch (final TransformException e) {
            throw new RuntimeException(e);
        }
        return elevationValues.asMap();
    }

}
