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

import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.*;

import java.util.Map;
import java.util.function.IntSupplier;

public class AbundanceAggregatingFadInitializer
    extends AggregatingFadInitializer<AbundanceLocalBiology, AbundanceAggregatingFad> {

    public AbundanceAggregatingFadInitializer(
        final GlobalBiology globalBiology,
        final FishAttractor<AbundanceLocalBiology, AbundanceAggregatingFad> fishAttractor,
        final IntSupplier timeStepSupplier,
        final CarryingCapacityInitializer<?> carryingCapacityInitializer,
        final Map<Species, Double> fishReleaseProbability
    ) {
        super(globalBiology, fishAttractor, timeStepSupplier, carryingCapacityInitializer, fishReleaseProbability);
    }

    @Override
    public AbundanceLocalBiology makeBiology(final GlobalBiology globalBiology) {
        return new AbundanceLocalBiology(globalBiology);
    }

    @Override
    protected AbundanceAggregatingFad makeFad(
        final FadManager owner,
        final AbundanceLocalBiology biology,
        final FishAttractor<AbundanceLocalBiology, AbundanceAggregatingFad> fishAttractor,
        final int stepDeployed,
        final Int2D locationDeployed,
        final CarryingCapacity carryingCapacity,
        final Map<Species, Double> fishReleaseProbabilities
    ) {
        return new AbundanceAggregatingFad(
            owner,
            biology,
            fishAttractor,
            stepDeployed,
            locationDeployed,
            carryingCapacity,
            fishReleaseProbabilities
        );
    }
}
