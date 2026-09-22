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

/**
 * A rule computing the next-step biomass for a location, given its current biomass and the
 * carrying capacity it is growing towards. {@code currentBiomass} and {@code carryingCapacity} may
 * refer to a single cell ({@link IndependentBiomassGrower}) or a whole grid's totals
 * ({@link CommonBiomassGrower}), depending on the grower applying the rule.
 */
public interface BiomassGrowthRule {
    /**
     * @param currentBiomass    the current biomass
     * @param carryingCapacity  the carrying capacity being grown towards
     * @return the biomass after one growth step
     */
    double newBiomass(
        double currentBiomass,
        double carryingCapacity
    );
}
