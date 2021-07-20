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

import static uk.ac.ox.oxfish.model.StepOrder.DAWN;

import java.util.Map;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;

/**
 *
 */
public abstract class Reallocator<K> implements Steppable, AdditionalStartable {

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
                final NauticalMap nauticalMap = fishState.getMap();
                final GlobalBiology globalBiology = fishState.getBiology();
                performReallocation(globalBiology, nauticalMap, grids);
            });
    }

    abstract void performReallocation(
        final GlobalBiology globalBiology,
        final NauticalMap nauticalMap,
        final Map<K, ? extends DoubleGrid2D> grids
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
        fishState.scheduleEveryStep(this, DAWN);
    }
}
