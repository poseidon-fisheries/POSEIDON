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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A class that reallocates fish around the map by using allocation grids.
 *
 * @param <K> The type of key used to identify the allocation grid to use.
 * @param <B> The type of local biology operated on.
 */
public abstract class Reallocator<K, B extends LocalBiology> implements BiologicalProcess<B> {

    private final AllocationGrids<K> allocationGrids;

    Reallocator(final AllocationGrids<K> allocationGrids) {
        this.allocationGrids = allocationGrids;
    }

    public AllocationGrids<K> getAllocationGrids() {
        return allocationGrids;
    }

    @Override
    public Optional<B> process(final FishState fishState, final B aggregatedBiology) {
        reallocate(
            fishState.getStep(),
            fishState.getBiology(),
            fishState.getMap().getAllSeaTilesExcludingLandAsList(),
            aggregatedBiology
        );
        // Reallocation doesn't return a new biology, since the given one
        // has been redistributed throughout the map. It should always be
        // the last operation in the biological process chain anyway.
        return Optional.empty();
    }

    /**
     * Performs reallocation by using the appropriate grids for the specified step.
     *
     * @param step              The simulation step that will determine which grid to use.
     * @param globalBiology     The global biology that will be used to map species names to {@link
     *                          uk.ac.ox.oxfish.biology.Species} objects.
     * @param seaTiles          The list of sea tiles to use as targets for reallocation.
     * @param aggregatedBiology The summary biology that contains all the fish to be reallocated.
     */
    public void reallocate(
        final int step,
        final GlobalBiology globalBiology,
        final List<SeaTile> seaTiles,
        final B aggregatedBiology
    ) {
        reallocate(
            allocationGrids.atOrBeforeStep(step),
            globalBiology,
            seaTiles,
            aggregatedBiology
        );
    }

    /**
     * Performs reallocation by using the specified grids.
     *
     * @param allocationGrids   The grids to use to perform reallocation.
     * @param globalBiology     The global biology that will be used to map species names to {@link
     *                          uk.ac.ox.oxfish.biology.Species} objects.
     * @param seaTiles          The list of sea tiles to use as targets for reallocation.
     * @param aggregatedBiology The summary biology that contains all the fish to be reallocated.
     */
    protected abstract void reallocate(
        Map<K, DoubleGrid2D> allocationGrids,
        GlobalBiology globalBiology,
        List<SeaTile> seaTiles,
        final B aggregatedBiology
    );

}
