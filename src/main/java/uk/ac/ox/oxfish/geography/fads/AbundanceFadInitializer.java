/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.geography.fads;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.IntSupplier;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.AbundanceFilter;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadBiomassAttractor;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;

class AbundanceFadInitializer extends FadInitializer<AbundanceLocalBiology, AbundanceFad> {

    private final Map<Species, AbundanceFilter> selectivityFilters;

    AbundanceFadInitializer(
        final GlobalBiology globalBiology,
        final double totalCarryingCapacity,
        final Map<Species, FadBiomassAttractor> fadBiomassAttractors,
        final double fishReleaseProbability,
        final IntSupplier timeStepSupplier,
        final Map<Species, AbundanceFilter> selectivityFilters
    ) {
        super(
            globalBiology,
            totalCarryingCapacity,
            fadBiomassAttractors,
            fishReleaseProbability,
            timeStepSupplier
        );
        this.selectivityFilters = ImmutableMap.copyOf(selectivityFilters);
    }

    @Override
    AbundanceLocalBiology makeBiology(final GlobalBiology globalBiology) {
        return new AbundanceLocalBiology(globalBiology);
    }

    @Override
    AbundanceFad makeFad(
        final FadManager<AbundanceLocalBiology, AbundanceFad> owner,
        final AbundanceLocalBiology biology,
        final Map<Species, FadBiomassAttractor> fadBiomassAttractors,
        final double fishReleaseProbability,
        final int stepDeployed,
        final Int2D locationDeployed
    ) {
        return new AbundanceFad(
            owner,
            biology,
            fadBiomassAttractors,
            fishReleaseProbability,
            stepDeployed,
            locationDeployed,
            selectivityFilters,
            getTotalCarryingCapacity()
        );
    }
}
