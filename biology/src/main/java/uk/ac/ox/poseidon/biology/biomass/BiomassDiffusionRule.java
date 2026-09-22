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

package uk.ac.ox.poseidon.biology.biomass;

import sim.util.Double2D;

/**
 * Computes how much biomass moves between a pair of neighbouring cells (conventionally X and Y)
 * for one diffusion step, given each cell's current biomass and carrying capacity.
 */
public interface BiomassDiffusionRule {
    /**
     * @param currentBiomassX   cell X's current biomass
     * @param carryingCapacityX cell X's carrying capacity
     * @param currentBiomassY   cell Y's current biomass
     * @param carryingCapacityY cell Y's carrying capacity
     * @return the (X, Y) biomasses after this diffusion step
     */
    Double2D updatedBiomasses(
        double currentBiomassX,
        double carryingCapacityX,
        double currentBiomassY,
        double carryingCapacityY
    );
}
