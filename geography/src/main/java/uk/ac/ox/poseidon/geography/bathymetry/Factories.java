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

public class Factories {

    private Factories() {}

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

    public static <S extends Scope> BathymetricGridFromElevationValuesFactory<S>
    bathymetricGridFromElevationValues(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final List<Number> elevationValues
    ) {
        return new BathymetricGridFromElevationValuesFactory<>(
            modelGrid, elevationValues
        );
    }

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

    public static <S extends Scope> UniformBathymetricGridFactory<S> uniformBathymetricGrid(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final double depth
    ) {
        return new UniformBathymetricGridFactory<>(modelGrid, depth);
    }
}
