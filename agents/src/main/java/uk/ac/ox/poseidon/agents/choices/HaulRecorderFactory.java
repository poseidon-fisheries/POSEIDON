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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.tasks.fishing.FishingEvent;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * A {@link VesselScopeFactory} counterpart of {@link HaulRecorder}, built via
 * {@link Factories#haulRecorder(Factory, Factory, Factory)}. It only builds the recorder: it does
 * not register it with the vessel's event manager or anything else, since a vessel can have
 * several recorders, each registered with whatever gives it the hauls it records.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HaulRecorderFactory extends VesselScopeFactory<HaulRecorder> {

    private Factory<? super VesselScope, ? extends ModelGrid> modelGrid;
    private Factory<
        ? super VesselScope,
        ? extends Function<? super FishingEvent, ? extends Memory<Int2D, Bucket>>
        > memorySelector;
    private Factory<? super VesselScope, ? extends BinaryOperator<Bucket>> updateRule;

    @Override
    protected HaulRecorder newInstance(final VesselScope scope) {
        return new HaulRecorder(
            modelGrid.get(scope),
            memorySelector.get(scope),
            updateRule.get(scope)
        );
    }
}
