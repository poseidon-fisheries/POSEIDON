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

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.model.StepOrder.DAWN;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.IntUnaryOperator;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;

public class ScheduledBiologicalProcesses<B extends LocalBiology, A extends Aggregator<B>>
    implements Steppable, AdditionalStartable {

    private final A aggregator;
    private final IntUnaryOperator stepMapper;
    private final Map<Integer, Collection<BiologicalProcess<B>>> schedule;

    ScheduledBiologicalProcesses(
        final A aggregator,
        final IntUnaryOperator stepMapper,
        final Map<Integer, Collection<BiologicalProcess<B>>> schedule
    ) {
        this.aggregator = aggregator;
        this.stepMapper = stepMapper;
        this.schedule = schedule.entrySet().stream().collect(toImmutableMap(
            Entry::getKey,
            entry -> ImmutableList.copyOf(entry.getValue())
        ));
    }

    public A getAggregator() {
        return aggregator;
    }

    /**
     * This is meant to be executed every step, but will only do the reallocation if we have one
     * scheduled on that step.
     */
    @Override
    public void step(final SimState simState) {
        final FishState fishState = (FishState) simState;

        final Collection<BiologicalProcess<B>> biologicalProcesses =
            schedule.get(stepMapper.applyAsInt(fishState.getStep()));

        if (biologicalProcesses != null) {
            B biology = aggregator.aggregate(fishState.getBiology(), fishState.getMap(), null);
            for (final BiologicalProcess<B> process : biologicalProcesses) {
                biology = process.process(fishState, biology).orElse(biology);
            }
        }
    }

    @Override
    public void start(final FishState fishState) {
        fishState.scheduleEveryStep(this, DAWN);
    }

}