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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import static com.google.common.base.Preconditions.checkNotNull;
import static uk.ac.ox.oxfish.model.StepOrder.DAWN;
import static uk.ac.ox.oxfish.model.StepOrder.POLICY_UPDATE;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateSteppable;

abstract class Restorer<K, A, B extends LocalBiology>
    implements AdditionalStartable {

    protected final Map<Integer, Integer> schedule;

    private final Reallocator<K, A> reallocator;
    private final Aggregator<A, B> aggregator;

    Restorer(
        final Reallocator<K, A> reallocator,
        final Aggregator<A, B> aggregator,
        final Map<Integer, Integer> schedule
    ) {
        this.reallocator = reallocator;
        this.aggregator = aggregator;
        this.schedule = ImmutableMap.copyOf(schedule);
    }

    @Override
    public void start(final FishState fishState) {
        schedule.forEach((recordingStep, restoringStep) ->
            // take our snapshots at dawn, before anything else happens
            schedule(fishState, recordingStep, DAWN, fishState1 -> {
                final Map<Species, A> aggregations = aggregator
                    .aggregate(fishState1.getBiology(), fishState.getMap(), null);
                // Schedule the restoration at POLICY_UPDATE step order so it runs after the
                // grower at BIOLOGY_PHASE step order, but before the data gatherers at later orders
                schedule(fishState1, restoringStep, POLICY_UPDATE, fishState2 ->
                    restoreAggregations(aggregations, fishState2)
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

    private void restoreAggregations(
        final Map<Species, A> aggregations,
        final FishState fishState
    ) {

        final GlobalBiology globalBiology = fishState.getBiology();
        final NauticalMap nauticalMap = fishState.getMap();
        final FadMap fadMap = checkNotNull(fishState.getFadMap());

        // we need to subtract the biomass that's currently under FADs
        // in order to avoid reallocating it
        final Map<Species, A> aggregationUnderFads =
            aggregator.aggregate(globalBiology, null, fadMap);

        final Map<Species, A> aggregationToReallocate =
            subtract(aggregations, aggregationUnderFads);

        reallocator.reallocate(
            fishState.getStep(),
            globalBiology,
            fishState.getMap().getAllSeaTilesExcludingLandAsList(),
            aggregationToReallocate
        );
    }

    abstract Map<Species, A> subtract(
        final Map<Species, A> initialAggregations,
        final Map<Species, A> aggregationsToSubtract
    );

}
