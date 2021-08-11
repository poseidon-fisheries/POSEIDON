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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * The factory in charge of creating an {@link AbundanceRestorer}. The {@link
 * #setAbundanceReallocator(AbundanceReallocator)} method needs to be called before {@link
 * #apply(FishState)}.
 */
public class AbundanceRestorerFactory implements AlgorithmFactory<AbundanceRestorer> {

    private ImmutableMap<Integer, Integer> schedule;
    private AbundanceReallocator abundanceReallocator;

    public AbundanceRestorerFactory(final ImmutableMap<Integer, Integer> schedule) {
        this.schedule = schedule;
    }

    public void setAbundanceReallocator(final AbundanceReallocator abundanceReallocator) {
        this.abundanceReallocator = abundanceReallocator;
    }

    @SuppressWarnings("unused")
    public ImmutableMap<Integer, Integer> getSchedule() {
        return schedule;
    }

    public void setSchedule(final ImmutableMap<Integer, Integer> schedule) {
        this.schedule = schedule;
    }

    @Override
    public AbundanceRestorer apply(final FishState fishState) {
        checkNotNull(abundanceReallocator, "need to call setAbundanceReallocator before using");
        return new AbundanceRestorer(
            abundanceReallocator,
            new AbundanceAggregator(false, true),
            schedule
        );
    }
}
