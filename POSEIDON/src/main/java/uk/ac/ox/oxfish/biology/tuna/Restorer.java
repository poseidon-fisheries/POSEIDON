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

import static uk.ac.ox.oxfish.model.StepOrder.DAWN;
import static uk.ac.ox.oxfish.model.StepOrder.POLICY_UPDATE;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateSteppable;

/**
 * The job of this class is to take an aggregated "snapshot" of the biology at some point in time
 * and restore it at a later point in time.
 *
 * <p>It is similar to the various {@link uk.ac.ox.oxfish.biology.complicated.BiologyResetter}
 * implementations, but there are a few key differences:
 * <ul>
 *  <li>It takes FADs into account.</li>
 *  <li>It's generic: subclasses can work with different types of local biologies.</li>
 *  <li>It relies on a {@link Reallocator} to redistribute the biology on the map
 *      when restoring it.</li>
 * </ul>
 *
 * @param <K> the type of key needed to identify which allocation map to use.
 * @param <B> the type of local biology to work with.
 */
abstract class Restorer<K, B extends LocalBiology>
    implements AdditionalStartable {

    private final Map<Integer, Integer> schedule;
    private final Reallocator<K, B> reallocator;
    private final Aggregator<B> aggregator;
    private final Extractor<B> extractor;
    private final Excluder<B> excluder;
    Restorer(
        final Reallocator<K, B> reallocator,
        final Aggregator<B> aggregator,
        final Extractor<B> extractor,
        final Excluder<B> excluder,
        final Map<Integer, Integer> schedule
    ) {
        this.reallocator = reallocator;
        this.aggregator = aggregator;
        this.extractor = extractor;
        this.excluder = excluder;
        this.schedule = ImmutableMap.copyOf(schedule);
    }

    public Extractor<B> getExtractor() {
        return extractor;
    }

    public Reallocator<K, B> getReallocator() {
        return reallocator;
    }

    public Aggregator<B> getAggregator() {
        return aggregator;
    }

    @Override
    public void start(final FishState fishState) {
        schedule.forEach((recordingStep, restoringStep) ->
            // take our snapshots at dawn, before anything else happens
            schedule(fishState, recordingStep, DAWN, fishState1 -> {
                final B aggregatedBiology =
                    aggregator.apply(fishState1.getBiology(), extractor.apply(fishState1));
                // Schedule the restoration at POLICY_UPDATE step order so it runs after the
                // grower at BIOLOGY_PHASE step order, but before the data gatherers at later orders
                schedule(fishState1, restoringStep, POLICY_UPDATE, fishState2 ->
                    restore(aggregatedBiology, fishState2)
                );
            })
        );
    }

    private static void schedule(
        final FishState fishState,
        final int step,
        final StepOrder stepOrder,
        @SuppressWarnings("TypeMayBeWeakened") // we want our steppable to be a functional interface
        final FishStateSteppable stepper
    ) {
        fishState.schedule.scheduleOnce(step, stepOrder.ordinal(), stepper);
    }

    private void restore(
        final B aggregatedBiology,
        final FishState fishState
    ) {
        reallocator.reallocate(
            fishState.getStep(),
            fishState.getBiology(),
            fishState.getMap().getAllSeaTilesExcludingLandAsList(),
            // we need to subtract the biomass that's currently under FADs
            // in order to avoid reallocating it
            excluder.exclude(aggregatedBiology, fishState)
        );
    }

}
