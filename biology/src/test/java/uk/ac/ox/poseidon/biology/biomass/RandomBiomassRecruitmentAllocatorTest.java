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

package uk.ac.ox.poseidon.biology.biomass;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.BaseDoubleGrid;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

class RandomBiomassRecruitmentAllocatorTest {

    private static final double EPSILON = 1e-9;
    private static final Species SPECIES = new Species("COD", null, "Cod");

    @Test
    void allocate_usesRecruitedBiomassWhenCapacityAvailable() {
        final ModelGrid modelGrid = newGrid(2, 2);
        final DefaultBiomassGrid biomassGrid = newBiomassGrid(
            modelGrid,
            new double[][]{
                {0.0, 0.0},
                {0.0, 0.0}
            }
        );
        final CarryingCapacityGrid capacityGrid = newCapacityGrid(
            modelGrid,
            new double[][]{
                {5.0, 5.0},
                {5.0, 5.0}
            }
        );

        final RandomBiomassRecruitmentAllocator allocator =
            new RandomBiomassRecruitmentAllocator(new MersenneTwisterFast(123));
        allocator.allocate(10.0, biomassGrid, capacityGrid);

        final double totalBiomass = modelGrid.getAllCells()
            .mapToDouble(biomassGrid::getValue)
            .sum();
        assertThat(totalBiomass).isCloseTo(10.0, offset(EPSILON));
        modelGrid.getAllCells().forEach(cell ->
            assertThat(biomassGrid.getValue(cell))
                .isLessThanOrEqualTo(capacityGrid.getCarryingCapacity(cell) + EPSILON)
        );
    }

    @Test
    void allocate_capsAtTotalCapacityWhenRecruitedExceedsAvailable() {
        final ModelGrid modelGrid = newGrid(2, 2);
        final DefaultBiomassGrid biomassGrid = newBiomassGrid(
            modelGrid,
            new double[][]{
                {1.0, 0.0},
                {0.0, 0.5}
            }
        );
        final CarryingCapacityGrid capacityGrid = newCapacityGrid(
            modelGrid,
            new double[][]{
                {1.5, 1.0},
                {0.5, 1.0}
            }
        );

        final double initialBiomass = modelGrid.getAllCells()
            .mapToDouble(biomassGrid::getValue)
            .sum();
        final double totalCapacity = modelGrid.getAllCells()
            .mapToDouble(capacityGrid::getCarryingCapacity)
            .sum();
        final double recruited = totalCapacity - initialBiomass + 3.0;

        final RandomBiomassRecruitmentAllocator allocator =
            new RandomBiomassRecruitmentAllocator(new MersenneTwisterFast(456));
        allocator.allocate(recruited, biomassGrid, capacityGrid);

        final double totalBiomass = modelGrid.getAllCells()
            .mapToDouble(biomassGrid::getValue)
            .sum();
        assertThat(totalBiomass).isCloseTo(totalCapacity, offset(EPSILON));
        modelGrid.getAllCells().forEach(cell ->
            assertThat(biomassGrid.getValue(cell))
                .isLessThanOrEqualTo(capacityGrid.getCarryingCapacity(cell) + EPSILON)
        );
    }

    @Test
    void allocate_throwsWhenNoHabitableCells() {
        final ModelGrid modelGrid = newGrid(1, 1);
        final DefaultBiomassGrid biomassGrid = newBiomassGrid(
            modelGrid,
            new double[][]{{0.0}}
        );
        final CarryingCapacityGrid capacityGrid = newCapacityGrid(
            modelGrid,
            new double[][]{{0.0}}
        );

        final RandomBiomassRecruitmentAllocator allocator =
            new RandomBiomassRecruitmentAllocator(new MersenneTwisterFast(789));

        assertThatThrownBy(() -> allocator.allocate(1.0, biomassGrid, capacityGrid))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No habitable cells");
    }

    private static ModelGrid newGrid(final int width, final int height) {
        return ModelGrid.create(width, height, new Envelope(0, width, 0, height));
    }

    private static DefaultBiomassGrid newBiomassGrid(
        final ModelGrid modelGrid,
        final double[][] values
    ) {
        return new DefaultBiomassGrid(modelGrid, SPECIES, values);
    }

    private static CarryingCapacityGrid newCapacityGrid(
        final ModelGrid modelGrid,
        final double[][] values
    ) {
        return new CarryingCapacityGrid(new BaseDoubleGrid(modelGrid, values));
    }
}
