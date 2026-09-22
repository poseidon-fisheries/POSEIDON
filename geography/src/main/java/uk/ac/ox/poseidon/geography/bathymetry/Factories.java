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

package uk.ac.ox.poseidon.geography.bathymetry;

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.aggregators.Aggregator;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.utils.ElevationTable;

import java.nio.file.Path;
import java.util.List;

/**
 * Factories for {@link BathymetricGrid}s: built from real elevation data (a table, a raster grid
 * file, or literal values) or generated synthetically (uniform, or a randomly-shaped coastline).
 */
public class Factories {

    private Factories() {}

    /**
     * @param elevationTable factory for the table to read elevation samples from
     * @param modelGrid      factory for the grid this bathymetry is defined over
     * @param aggregator     factory for the aggregator combining a cell's samples
     * @param inverted       whether to negate every sample value
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link DefaultBathymetricGrid}
     * @see BathymetricGridFromElevationTableFactory
     */
    public static <S extends Scope> BathymetricGridFromElevationTableFactory<S>
    bathymetricGridFromElevationTable(
        final Factory<? super S, ? extends ElevationTable> elevationTable,
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final Factory<? super S, ? extends Aggregator> aggregator,
        final boolean inverted
    ) {
        return new BathymetricGridFromElevationTableFactory<>(
            elevationTable, modelGrid, aggregator, inverted
        );
    }

    /**
     * @param modelGrid       factory for the grid this bathymetry is defined over
     * @param elevationValues one literal elevation value per cell, in row-major (x-then-y) order
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link DefaultBathymetricGrid}
     * @see BathymetricGridFromElevationValuesFactory
     */
    public static <S extends Scope> BathymetricGridFromElevationValuesFactory<S>
    bathymetricGridFromElevationValues(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final List<Number> elevationValues
    ) {
        return new BathymetricGridFromElevationValuesFactory<>(
            modelGrid, elevationValues
        );
    }

    /**
     * @param path       factory for the raster grid file to read
     * @param modelGrid  factory for the grid this bathymetry is defined over
     * @param aggregator factory for the aggregator combining a cell's samples
     * @param inverted   whether to negate every sample value
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link DefaultBathymetricGrid}
     * @see BathymetricGridFromGridFileFactory
     */
    public static <S extends Scope> BathymetricGridFromGridFileFactory<S>
    bathymetricGridFromGridFile(
        final Factory<? super S, ? extends Path> path,
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final Factory<? super S, ? extends Aggregator> aggregator,
        final boolean inverted
    ) {
        return new BathymetricGridFromGridFileFactory<>(
            path, modelGrid, aggregator, inverted
        );
    }

    /**
     * @param modelGrid                        factory for the grid this bathymetry is defined
     *                                          over
     * @param coastalRoughness                 number of land-to-water flipping passes
     * @param smoothingIterations               number of elevation-smoothing passes
     * @param smoothingStrength                 how far each smoothing pass nudges a cell toward
     *                                          its neighbours' average elevation, from 0 to 1
     * @param maximumLandWidth                  the maximum width, in cells, of the initial land
     *                                          strip
     * @param minimumElevation                  the minimum (most negative) initial water
     *                                          elevation; must be negative
     * @param maximumElevation                  the maximum initial land elevation; must be
     *                                          positive
     * @param probabilityOfFlippingLandToWater  the per-cell, per-pass probability a land cell
     *                                          adjacent to water flips to water
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for a synthetic
     * {@link DefaultBathymetricGrid}
     * @see RoughCoastalBathymetricGridFactory
     */
    public static RoughCoastalBathymetricGridFactory roughCoastalBathymetricGrid(
        final Factory<? super SimulationScope, ? extends ModelGrid> modelGrid,
        final int coastalRoughness,
        final int smoothingIterations,
        final double smoothingStrength,
        final int maximumLandWidth,
        final double minimumElevation,
        final double maximumElevation,
        final double probabilityOfFlippingLandToWater
    ) {
        return new RoughCoastalBathymetricGridFactory(
            modelGrid,
            coastalRoughness,
            smoothingIterations,
            smoothingStrength,
            maximumLandWidth,
            minimumElevation,
            maximumElevation,
            probabilityOfFlippingLandToWater
        );
    }

    /**
     * @param modelGrid factory for the grid this bathymetry is defined over
     * @param depth     the depth every cell is set to (positive means water)
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a flat, all-water
     * {@link DefaultBathymetricGrid}
     * @see UniformBathymetricGridFactory
     */
    public static <S extends Scope> UniformBathymetricGridFactory<S> uniformBathymetricGrid(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final double depth
    ) {
        return new UniformBathymetricGridFactory<>(modelGrid, depth);
    }
}
