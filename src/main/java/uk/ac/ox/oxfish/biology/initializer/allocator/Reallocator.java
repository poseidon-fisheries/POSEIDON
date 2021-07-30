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

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.model.StepOrder.DAWN;

import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ObjectArrayMessage;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;

/**
 *
 */
public abstract class Reallocator<K> implements Steppable, AdditionalStartable {

    private static final Logger logger = LogManager.getLogger("biomass_events");

    final AllocationGrids<K> allocationGrids;
    final int period;

    /**
     * Constructs a new BiomassReallocator.
     *
     * @param allocationGrids The distribution grids used to reallocate biomass
     * @param period          The number to use as modulo for looping the schedule (normally 365)
     */
    Reallocator(
        final AllocationGrids<K> allocationGrids,
        final int period
    ) {
        this.allocationGrids = allocationGrids;
        this.period = period;
    }

    AllocationGrids<K> getAllocationGrids() {
        return allocationGrids;
    }

    public int getPeriod() {
        return period;
    }

    /**
     * This is meant to be executed every step, but will only do the reallocation if we have one
     * scheduled on that step.
     */
    @Override
    public void step(final SimState simState) {
        final FishState fishState = (FishState) simState;
        allocationGrids
            .atStep(fishState.getStep() % period)
            .ifPresent(grids -> {
                final Map<Species, Double> biomassBefore = fishState.getTotalBiomasses();
                final NauticalMap nauticalMap = fishState.getMap();
                final GlobalBiology globalBiology = fishState.getBiology();
                performReallocation(globalBiology, nauticalMap, grids);
                fishState.getTotalBiomasses().forEach((species, biomassAfter) ->
                    logger.debug(() -> new ObjectArrayMessage(
                        fishState.getStep(),
                        DAWN,
                        "REALLOCATE",
                        species.getName(),
                        biomassBefore.get(species),
                        biomassAfter
                    ))
                );
            });
    }

    abstract void performReallocation(
        final GlobalBiology globalBiology,
        final NauticalMap nauticalMap,
        final Map<K, DoubleGrid2D> grids
    );

    public void reallocate(
        final int step,
        final GlobalBiology globalBiology,
        final NauticalMap nauticalMap
    ) {
        allocationGrids
            .atOrBeforeStep(step % period)
            .ifPresent(grids -> performReallocation(
                globalBiology,
                nauticalMap,
                grids
            ));
    }

    @Override
    public void start(final FishState fishState) {
        assert allNonZeroGridCellsMapToVariableBiomassSeaTiles(fishState);
        fishState.scheduleEveryStep(this, DAWN);
    }

    /**
     * We need to make sure that all non-zero biomass grid cells in all our allocation grids
     * match an actual SeaTile with a VariableBiomassBasedBiology. Otherwise, even if the grid's
     * values sum up to 1.0, some biomass will be lost during the reallocation process. This is
     * a moderately costly check, but we only do it at the start of the simulation and wrap it
     * in an assert so it gets disabled when doing proper runs.
     */
    private boolean allNonZeroGridCellsMapToVariableBiomassSeaTiles(final FishState fishState) {
        final Set<Int2D> variableBiomassSeaTiles =
            fishState.getMap().getAllSeaTilesExcludingLandAsList()
                .stream()
                .filter(seaTile -> seaTile.getBiology() instanceof VariableBiomassBasedBiology)
                .map(seaTile -> new Int2D(seaTile.getGridX(), seaTile.getGridY()))
                .collect(toImmutableSet());
        return allocationGrids.getGrids().entrySet().stream().allMatch(entry1 ->
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
                return Sets.difference(nonZeroGridCells, variableBiomassSeaTiles).isEmpty();
            })
        );
    }
}
