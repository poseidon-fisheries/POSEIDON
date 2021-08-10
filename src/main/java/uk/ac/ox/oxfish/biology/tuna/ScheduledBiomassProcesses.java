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

import java.util.Collection;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.model.FishState;

/**
 * TODO.
 */
public class ScheduledBiomassProcesses extends
    ScheduledBiologicalProcesses<BiomassLocalBiology, BiomassAggregator> {

    ScheduledBiomassProcesses(
        final BiomassAggregator aggregator,
        final IntUnaryOperator stepMapper,
        final Map<Integer, Collection<BiologicalProcess<BiomassLocalBiology>>> schedule
    ) {
        super(aggregator, stepMapper, schedule);
    }

    @Override
    BiomassLocalBiology aggregate(final FishState fishState) {
        // Since the only scheduled biomass process is reallocation, we leave the FADs alone.
        // If that were to change, we'd need to aggregate everything and then exclude the FAD
        // biomass before reallocation like we're doing for the abundance processes.
        return getAggregator().aggregate(
            fishState.getBiology(),
            fishState.getMap(),
            null
        );
    }
}
