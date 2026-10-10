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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

/**
 * A {@link RelativeScopeFactory} for a {@link CommonBiomassGrower}. Built via
 * {@link Factories#commonBiomassGrower}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CommonBiomassGrowerFactory<S extends Scope>
    extends RelativeScopeFactory<S, CommonBiomassGrower> {

    private Factory<? super S, ? extends BiomassGrid> biomassGrid;
    private Factory<? super S, ? extends CarryingCapacityGrid> carryingCapacityGrid;
    private Factory<? super S, ? extends BiomassGrowthRule> biomassGrowthRule;
    private Factory<? super S, ? extends BiomassRecruitmentAllocator>
        biomassRecruitmentAllocator;

    @Override
    protected CommonBiomassGrower newInstance(final S scope) {
        return new CommonBiomassGrower(
            biomassGrid.get(scope),
            carryingCapacityGrid.get(scope),
            biomassGrowthRule.get(scope),
            biomassRecruitmentAllocator.get(scope)
        );
    }
}
