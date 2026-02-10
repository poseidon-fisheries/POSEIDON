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
import lombok.RequiredArgsConstructor;
import sim.util.Bag;
import sim.util.Int2D;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.min;
import static java.lang.System.Logger.Level.WARNING;

@RequiredArgsConstructor
public class RandomBiomassRecruitmentAllocator implements BiomassRecruitmentAllocator {

    private static final System.Logger logger =
        System.getLogger(RandomBiomassRecruitmentAllocator.class.getName());

    private final MersenneTwisterFast rng;

    @Override
    public void allocate(
        final double recruitedBiomassInKg,
        final BiomassGrid biomassGrid,
        final CarryingCapacityGrid carryingCapacityGrid
    ) {
        checkArgument(recruitedBiomassInKg >= 0, "Recruited biomass must be non-negative");
        final Bag cells = new Bag();
        cells.addAll(carryingCapacityGrid.getHabitableCells());
        checkArgument(!cells.isEmpty(), "No habitable cells");
        final double delta = recruitedBiomassInKg / cells.size();
        double biomassRemaining = recruitedBiomassInKg;
        while (biomassRemaining > 0 && !cells.isEmpty()) {
            final int i = rng.nextInt(cells.size());
            final Int2D selectedCell = (Int2D) cells.get(i);
            final double currentBiomass = biomassGrid.getValue(selectedCell);
            final double availableCapacity =
                carryingCapacityGrid.getCarryingCapacity(selectedCell) - currentBiomass;
            if (availableCapacity > 0) {
                final double allocatedBiomass = min(
                    availableCapacity,
                    min(delta, biomassRemaining)
                );
                biomassGrid.setBiomass(selectedCell, currentBiomass + allocatedBiomass);
                biomassRemaining -= allocatedBiomass;
                if (availableCapacity - allocatedBiomass <= 0) {
                    cells.remove(i);
                }
            } else {
                cells.remove(i);
            }
        }
        if (biomassRemaining > 0) {
            logger.log(
                WARNING,
                "Recruitment exceeded carrying capacity; unallocated biomass: "
                    + biomassRemaining
                    + " kg out of "
                    + recruitedBiomassInKg
                    + " kg"
            );
        }
    }
}
