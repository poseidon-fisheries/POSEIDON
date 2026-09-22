/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

import ec.util.MersenneTwisterFast;
import lombok.*;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * A {@link SimulationScopeFactory} for a synthetic {@link DefaultBathymetricGrid}, generated
 * (using the simulation's own random stream, so it varies run to run but not on repeated calls
 * within a run) rather than read from real data: a randomly-elevated ocean/land split along the
 * grid's x axis (land confined to a strip of at most {@code maximumLandWidth} columns), then
 * roughened by randomly flipping land cells adjacent to water to water
 * ({@code coastalRoughness} passes, each cell flipped with probability
 * {@code probabilityOfFlippingLandToWater}), then smoothed toward its neighbours' average
 * elevation ({@code smoothingIterations} passes, each nudging every cell by up to
 * {@code smoothingStrength} of the gap between its own and its neighbours' average elevation,
 * without crossing the water/land boundary). Built via
 * {@link Factories#roughCoastalBathymetricGrid(Factory, int, int, double, int, double, double,
 * double)}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoughCoastalBathymetricGridFactory
    extends SimulationScopeFactory<BathymetricGrid> {

    @NonNull private Factory<? super SimulationScope, ? extends ModelGrid> modelGrid;
    private int coastalRoughness;
    private int smoothingIterations;
    private double smoothingStrength;
    private int maximumLandWidth;
    private double minimumElevation;
    private double maximumElevation;
    private double probabilityOfFlippingLandToWater;

    /**
     * @throws IllegalStateException if {@code minimumElevation >= 0} or
     *                                {@code maximumElevation <= 0}
     */
    @Override
    protected BathymetricGrid newInstance(final SimulationScope scope) {
        checkState(minimumElevation < 0);
        checkState(maximumElevation > 0);

        final ModelGrid modelGrid = this.modelGrid.get(scope);
        final MersenneTwisterFast rng = scope.getSimulation().random;
        final DoubleGrid2D doubleGrid2D = new DoubleGrid2D(
            modelGrid.getGridWidth(),
            modelGrid.getGridHeight()
        );

        for (int x = 0; x < doubleGrid2D.width; x++)
            for (int y = 0; y < doubleGrid2D.height; y++)
                doubleGrid2D.field[x][y] =
                    x < doubleGrid2D.width - maximumLandWidth
                        ? rng.nextDouble() * minimumElevation
                        : rng.nextDouble() * maximumElevation;

        // The "roughing" of the coast consist in taking all land
        // locations and giving them a chance to flip to water if
        // at least one of their neighbours is water.
        for (int i = 0; i < coastalRoughness; i++) {
            // We wrap our doubleGrid2D in a temporary DefaultBathymetricGrid
            // just to take advantage of `getLandCells()` and
            // `getMooreLocations()`. We don't want to hold on to any
            // of those instances because we are directly mutating the
            // underlying array, which messes up the cached land/water
            // tile lists.
            final DefaultBathymetricGrid bathymetricGrid =
                new DefaultBathymetricGrid(modelGrid, doubleGrid2D);
            bathymetricGrid
                .getLandCells()
                .filter(cell ->
                    modelGrid
                        .getActiveNeighbours(cell)
                        .stream()
                        .anyMatch(bathymetricGrid::isWater)
                        && rng.nextBoolean(probabilityOfFlippingLandToWater)
                )
                .toList()
                .forEach(cell ->
                    doubleGrid2D.field[cell.x][cell.y] = rng.nextDouble() * minimumElevation
                );
        }

        for (int i = 0; i < smoothingIterations; i++) {
            final BathymetricGrid bathymetricGrid = new DefaultBathymetricGrid(
                modelGrid,
                doubleGrid2D
            );
            modelGrid
                .getAllCells()
                .collect(toMap(
                    identity(),
                    target -> {
                        final double oldElevation = bathymetricGrid.getElevation(target);
                        final double neighbourElevation = modelGrid
                            .getActiveNeighbours(target)
                            .stream()
                            .mapToDouble(bathymetricGrid::getElevation)
                            .average()
                            .orElseThrow();
                        final double boundedNeighbourElevation =
                            bathymetricGrid.isWater(target)
                                ? min(0, neighbourElevation)
                                : max(0, neighbourElevation);
                        return oldElevation +
                            rng.nextDouble() * smoothingStrength *
                                (boundedNeighbourElevation - oldElevation);
                    }
                ))
                .forEach((cell, newElevation) ->
                    doubleGrid2D.field[cell.x][cell.y] = newElevation
                );
        }
        return new DefaultBathymetricGrid(modelGrid, doubleGrid2D);
    }

}
