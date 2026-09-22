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

package uk.ac.ox.poseidon.geography.distance;

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

/**
 * Factories for {@link DistanceCalculator}s: geographic (equirectangular, haversine) and
 * grid-native (Cartesian) distance measures.
 */
public class Factories {

    private Factories() {}

    /**
     * @param modelGrid factory for the grid to measure distances over
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an
     * {@link EquirectangularDistanceCalculator}
     * @see EquirectangularDistanceCalculator
     */
    public static <S extends Scope> EquirectangularDistanceCalculatorFactory<S>
    equirectangularDistanceCalculator(
        final Factory<? super S, ? extends ModelGrid> modelGrid
    ) {
        return new EquirectangularDistanceCalculatorFactory<>(modelGrid);
    }

    /**
     * @param modelGrid factory for the grid to measure distances over
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link HaversineDistanceCalculator}
     * @see HaversineDistanceCalculator
     */
    public static <S extends Scope> HaversineDistanceCalculatorFactory<S>
    haversineDistanceCalculator(
        final Factory<? super S, ? extends ModelGrid> modelGrid
    ) {
        return new HaversineDistanceCalculatorFactory<>(modelGrid);
    }

    /**
     * @param modelGrid    factory for the grid to measure distances over
     * @param cellSizeInKm the length, in kilometres, of one grid cell's side
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link CartesianDistanceCalculator}
     * @see CartesianDistanceCalculator
     */
    public static <S extends Scope> CartesianDistanceCalculatorFactory<S>
    cartesianDistanceCalculator(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final double cellSizeInKm
    ) {
        return new CartesianDistanceCalculatorFactory<>(modelGrid, cellSizeInKm);
    }
}
