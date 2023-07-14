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

package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;

import java.util.Collection;

public class AbundanceCatchSamplersFactory extends CatchSamplersFactory<AbundanceLocalBiology> {

    private AbundanceFiltersFactory abundanceFiltersFactory;

    public AbundanceCatchSamplersFactory() {
    }

    public AbundanceCatchSamplersFactory(
        final AbundanceFiltersFactory abundanceFiltersFactory,
        final InputPath catchSamplesFile
    ) {
        super(catchSamplesFile);
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    @SuppressWarnings("unused")
    public AbundanceFiltersFactory getAbundanceFiltersFactory() {
        return abundanceFiltersFactory;
    }

    @SuppressWarnings("unused")
    public void setAbundanceFiltersFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    @Override
    CatchSampler<AbundanceLocalBiology> makeCatchSampler(
        final FishState fishState,
        final Class<? extends AbstractSetAction> actionClass,
        final Collection<Collection<Double>> sample,
        final MersenneTwisterFast rng
    ) {
        return new AbundanceCatchSampler(sample, rng, abundanceFiltersFactory.apply(fishState).get(actionClass));
    }

}
