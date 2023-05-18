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
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.IntUnaryOperator;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.model.StepOrder.DAWN;

/**
 * Objects of this class keep a schedule of biological processes to execute at specific time
 * indices. The actual time step of the simulation is mapped to a schedule index using the
 * stepMapper. Typically, the indices will be days of the year and the step mapper just a {@link
 * PeriodicStepMapper}.
 *
 * <p>If there are processes to be executed for the given time step, the processes are executed in
 * order. We maintain a {@code biology} variable (initially {@code null}) that is optionally updated
 * if the process returns a biology and passed to the next process. Some processes operate directly
 * on the {@link FishState} and ignore the provided biology. Other processes are called only for
 * their side effects and do not return a biology.
 *
 * <p>See the {@link ScheduledAbundanceProcessesFactory} for a good example use of this class.
 *
 * @param <B> The type of local biology to operate upon.
 */
class ScheduledBiologicalProcesses<B extends LocalBiology>
    implements Steppable, AdditionalStartable {

    private final IntUnaryOperator stepMapper;
    private final Map<Integer, Collection<BiologicalProcess<B>>> schedule;

    ScheduledBiologicalProcesses(
        final IntUnaryOperator stepMapper,
        final Map<Integer, Collection<BiologicalProcess<B>>> schedule
    ) {
        this.stepMapper = stepMapper;
        this.schedule = schedule.entrySet().stream().collect(toImmutableMap(
            Entry::getKey,
            entry -> ImmutableList.copyOf(entry.getValue())
        ));
    }

    /**
     * This is meant to be executed every step, but will only do the reallocation if we have one
     * scheduled on that step.
     */
    @Override
    public void step(final SimState simState) {
        final FishState fishState = (FishState) simState;
        if (fishState.getStep() > 0) {
            final Collection<BiologicalProcess<B>> biologicalProcesses =
                schedule.get(stepMapper.applyAsInt(fishState.getStep()));
            if (biologicalProcesses != null) {
                Collection<B> biologies = null;
                for (final BiologicalProcess<B> process : biologicalProcesses) {
                    biologies = process.process(fishState, biologies);
                }
            }
        }
    }

    @Override
    public void start(final FishState fishState) {
        fishState.scheduleEveryStep(this, DAWN);
    }

}
