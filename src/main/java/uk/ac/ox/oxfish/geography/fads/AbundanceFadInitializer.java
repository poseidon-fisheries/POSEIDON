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

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FishAttractor;

class AbundanceFadInitializer extends AbstractFadInitializer<AbundanceLocalBiology, AbundanceFad> {


    public AbundanceFadInitializer(GlobalBiology globalBiology, DoubleSupplier carryingCapacityGenerator,
                                   FishAttractor<AbundanceLocalBiology, AbundanceFad> fishAttractor,
                                   double fishReleaseProbability,
                                   IntSupplier timeStepSupplier) {
        super(globalBiology, carryingCapacityGenerator, fishAttractor, fishReleaseProbability, timeStepSupplier);
    }

    @Override
    public AbundanceLocalBiology makeBiology(final GlobalBiology globalBiology) {
        return new AbundanceLocalBiology(globalBiology);
    }

    @Override
    public AbundanceFad makeFad(
        final FadManager<AbundanceLocalBiology, AbundanceFad> owner,
        final AbundanceLocalBiology biology,
        final FishAttractor<AbundanceLocalBiology, AbundanceFad> fishAttractor,
        final double fishReleaseProbability,
        final int stepDeployed,
        final Int2D locationDeployed
    ) {
        return new AbundanceFad(
            owner,
            biology,
            fishAttractor,
            fishReleaseProbability,
            stepDeployed,
            locationDeployed,
            generateCarryingCapacity()
        );
    }
}
