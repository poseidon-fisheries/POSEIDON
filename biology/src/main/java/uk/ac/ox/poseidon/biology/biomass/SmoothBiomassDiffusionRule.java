/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.biology.biomass;

import lombok.Data;
import sim.util.Double2D;

@Data
public class SmoothBiomassDiffusionRule implements BiomassDiffusionRule {

    /**
     * how much of the differential do you want to move
     */
    private final double differentialPercentageToMove;

    /**
     * maximum percentage of biomass that can leave one place for another
     */
    private final double percentageLimitOnDailyMovement;

    @Override
    public Double2D updatedBiomasses(
        final double currentBiomassX,
        final double carryingCapacityX,
        final double currentBiomassY,
        final double carryingCapacityY
    ) {
        final double delta = currentBiomassX - currentBiomassY;
        if (delta > 0) {
            final double differential = Math.min(delta, (carryingCapacityY - currentBiomassY));
            if (differential > 0) {
                final double movement = Math.min(
                    differentialPercentageToMove * differential,
                    percentageLimitOnDailyMovement * currentBiomassX
                );
                return new Double2D(currentBiomassX - movement, currentBiomassY + movement);
            } else return new Double2D(currentBiomassX, currentBiomassY);
        } else return new Double2D(currentBiomassX, currentBiomassY);
    }
}
