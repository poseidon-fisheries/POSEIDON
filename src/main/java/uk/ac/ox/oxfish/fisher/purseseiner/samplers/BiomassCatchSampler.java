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
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;

public class BiomassCatchSampler extends CatchSampler<BiomassLocalBiology> {

    public BiomassCatchSampler(
        Collection<Collection<Double>> sample,
        MersenneTwisterFast rng
    ) {
        super(sample, rng);
    }

    @Override
    public BiomassLocalBiology apply(final BiomassLocalBiology biology) {
        @SuppressWarnings("UnstableApiUsage") final double[] biomassCaught =
            next(biology.getCurrentBiomass()).toArray();
        // reuse the biomassCaught array for carrying capacity, since it doesn't
        // make sense to have "extra" carrying capacity for a school of fish
        return new BiomassLocalBiology(biomassCaught, biomassCaught);
    }

}
