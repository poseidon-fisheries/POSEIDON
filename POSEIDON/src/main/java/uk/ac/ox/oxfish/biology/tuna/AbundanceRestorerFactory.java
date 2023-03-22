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

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The factory in charge of creating an {@link AbundanceRestorer}.
 */
public class AbundanceRestorerFactory
    extends RestorerFactory<AbundanceLocalBiology> {

    private Map<Integer, Integer> schedule;

    /**
     * Empty constructor for YAML instantiation
     */
    @SuppressWarnings("unused")
    public AbundanceRestorerFactory() {
    }

    public AbundanceRestorerFactory(final Map<Integer, Integer> schedule) {
        this.schedule = ImmutableMap.copyOf(schedule);
    }

    @SuppressWarnings("unused")
    public Map<Integer, Integer> getSchedule() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return schedule;
    }

    public void setSchedule(final Map<Integer, Integer> schedule) {
        this.schedule = ImmutableMap.copyOf(schedule);
    }

    @Override
    public AbundanceRestorer apply(final FishState fishState) {
        checkNotNull(getReallocator(), "need to call setAbundanceReallocator before using");
        return new AbundanceRestorer(
            getReallocator(),
            new AbundanceAggregator(),
            schedule
        );
    }
}
