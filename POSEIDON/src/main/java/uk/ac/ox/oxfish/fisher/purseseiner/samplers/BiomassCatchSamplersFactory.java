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
import java.util.Collection;
import java.util.function.Supplier;

import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.model.FishState;

public class BiomassCatchSamplersFactory extends CatchSamplersFactory<BiomassLocalBiology> {

    @SuppressWarnings("unused")
    public BiomassCatchSamplersFactory() {
    }

    public BiomassCatchSamplersFactory(final Supplier<SpeciesCodes> speciesCodesSupplier) {
        super(speciesCodesSupplier);
    }

    @Override
    CatchSampler<BiomassLocalBiology> makeCatchSampler(
        FishState fishState, Class<? extends AbstractSetAction<?>> actionClass,
        final Collection<Collection<Double>> sample,
        final MersenneTwisterFast rng
    ) {
        return new BiomassCatchSampler(sample, rng);
    }
}
