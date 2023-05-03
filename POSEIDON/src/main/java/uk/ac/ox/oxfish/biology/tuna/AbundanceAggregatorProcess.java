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

package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A simple wrapper around {@link AbundanceAggregator} in order to use it in a {@link
 * BiologicalProcess} chain.
 */
public class AbundanceAggregatorProcess implements BiologicalProcess<AbundanceLocalBiology> {

    private final AbundanceAggregator abundanceAggregator = new AbundanceAggregator();

    @Override
    public Collection<AbundanceLocalBiology> process(
        final FishState fishState,
        final Collection<AbundanceLocalBiology> biologies
    ) {
        return ImmutableList.of(abundanceAggregator.apply(fishState.getBiology(), biologies));
    }
}
