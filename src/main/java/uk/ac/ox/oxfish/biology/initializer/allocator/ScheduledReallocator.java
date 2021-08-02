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

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.model.StepOrder.DAWN;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.function.IntPredicate;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;

public class ScheduledReallocator<K, A, B extends LocalBiology, G extends Aggregator<A, B>, R extends Reallocator<K, A>>
    implements Steppable, AdditionalStartable {

    private final R reallocator;
    private final G aggregator;
    private final IntPredicate schedule;
    public ScheduledReallocator(
        final R reallocator,
        final G aggregator,
        final IntPredicate schedule
    ) {
        this.reallocator = reallocator;
        this.aggregator = aggregator;
        this.schedule = schedule;
    }

    public G getAggregator() {
        return aggregator;
    }

    public R getReallocator() {
        return reallocator;
    }

    /**
     * This is meant to be executed every step, but will only do the reallocation if we have one
     * scheduled on that step.
     */
    @Override
    public void step(final SimState simState) {
        final FishState fishState = (FishState) simState;
        if (schedule.test(fishState.getStep())) {
            reallocator.reallocate(
                fishState.getStep(),
                fishState.getBiology(),
                fishState.getMap().getAllSeaTilesExcludingLandAsList(),
                aggregator.aggregate(fishState.getBiology(), fishState.getMap(), null)
            );
        }
    }


    @Override
    public void start(final FishState fishState) {
        assert allNonZeroGridCellsMapRightBiologySeaTiles(fishState);
        fishState.scheduleEveryStep(this, DAWN);
    }

    /**
     * We need to make sure that all non-zero biomass grid cells in all our allocation grids match
     * an actual SeaTile with the right biology (BiomassLocalBiology or AbundanceLocalBiology)
     * Otherwise, even if the grid's values sum up to 1.0, some biomass will be lost during the
     * reallocation process. This is a moderately costly check, but we only do it at the start of
     * the simulation and wrap it in an assert so it gets disabled when doing proper runs.
     */
    private boolean allNonZeroGridCellsMapRightBiologySeaTiles(final FishState fishState) {

        final Set<Int2D> rightBiologySeaTiles =
            fishState.getMap().getAllSeaTilesExcludingLandAsList()
                .stream()
                .filter(seaTile ->
                    aggregator.getLocalBiologyClass().isInstance(seaTile.getBiology())
                )
                .map(seaTile -> new Int2D(seaTile.getGridX(), seaTile.getGridY()))
                .collect(toImmutableSet());
        return reallocator.getAllocationGrids().getGrids().entrySet().stream().allMatch(entry1 ->
            entry1.getValue().entrySet().stream().allMatch(entry2 -> {
                final DoubleGrid2D grid = entry2.getValue();
                final Set<Int2D> nonZeroGridCells =
                    range(0, grid.field.length).boxed()
                        .flatMap(x ->
                            range(0, grid.field[x].length)
                                .filter(y -> grid.get(x, y) > 0)
                                .boxed()
                                .map(y -> new Int2D(x, y))
                        )
                        .collect(toImmutableSet());
                return Sets.difference(nonZeroGridCells, rightBiologySeaTiles).isEmpty();
            })
        );
    }

}
