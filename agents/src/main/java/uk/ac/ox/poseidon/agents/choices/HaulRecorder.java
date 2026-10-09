/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.poseidon.agents.choices;

import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.tasks.fishing.FishingEvent;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.core.events.AbstractListener;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.util.function.BinaryOperator;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Records each haul it receives in a {@link Memory}: the haul's retained catch, observed in the
 * cell where the haul started, in the memory selected from the haul (such as the slice for the
 * gear it was made with), revised with an update rule.
 * <p>
 * It does not register itself with anything: whatever it is registered with decides whose hauls
 * it records. A vessel can have several, on the same memory, with different update rules, such
 * as one for its own hauls and one for hauls it is told about.
 */
public class HaulRecorder extends AbstractListener<FishingEvent> {

    private final ModelGrid modelGrid;
    private final Function<? super FishingEvent, ? extends Memory<Int2D, Bucket>> memorySelector;
    private final BinaryOperator<Bucket> updateRule;

    /**
     * @param modelGrid      the grid giving the cell of a haul's coordinate
     * @param memorySelector selects, from a haul, the memory to record it in
     * @param updateRule     revises what is remembered of a cell with a new observation of it
     */
    HaulRecorder(
        final ModelGrid modelGrid,
        final Function<? super FishingEvent, ? extends Memory<Int2D, Bucket>> memorySelector,
        final BinaryOperator<Bucket> updateRule
    ) {
        super(FishingEvent.class);
        this.modelGrid = checkNotNull(modelGrid);
        this.memorySelector = checkNotNull(memorySelector);
        this.updateRule = checkNotNull(updateRule);
    }

    @Override
    public void receive(final FishingEvent event) {
        memorySelector.apply(event).observe(
            modelGrid.toCell(event.getAction().getStartCoordinate()),
            event.getOutcome().getDisposition().getRetained(),
            updateRule
        );
    }
}
