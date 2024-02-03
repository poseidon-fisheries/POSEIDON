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
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

import java.util.Collection;

public class AbundanceCatchSamplersFactory extends CatchSamplersFactory<AbundanceLocalBiology> {

    private AbundanceFiltersFactory abundanceFilters;

    public AbundanceCatchSamplersFactory() {
    }

    public AbundanceCatchSamplersFactory(
        final AbundanceFiltersFactory abundanceFilters,
        final InputPath catchSamplesFile,
        final IntegerParameter targetYear
    ) {
        super(catchSamplesFile, targetYear);
        this.abundanceFilters = abundanceFilters;
    }

    @SuppressWarnings("unused")
    public AbundanceFiltersFactory getAbundanceFilters() {
        return abundanceFilters;
    }

    @SuppressWarnings("unused")
    public void setAbundanceFilters(final AbundanceFiltersFactory abundanceFilters) {
        this.abundanceFilters = abundanceFilters;
    }

    @Override
    CatchSampler<AbundanceLocalBiology> makeCatchSampler(
        final FishState fishState,
        final Class<? extends AbstractSetAction> actionClass,
        final Collection<Collection<Double>> sample,
        final MersenneTwisterFast rng
    ) {
        return new AbundanceCatchSampler(sample, rng, abundanceFilters.apply(fishState).get(actionClass));
    }

}
