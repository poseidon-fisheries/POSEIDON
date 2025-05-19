/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.core.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sim.engine.Steppable;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ScheduledRepeatingFactory<C extends Steppable> implements Factory<C> {

    private Factory<? extends Temporal> startingDateTime;
    private Factory<? extends TemporalAmount> interval;
    private Factory<? extends C> steppable;
    private int ordering;

    @Override
    public C get(final Simulation simulation) {
        final C steppableObject = steppable.get(simulation);
        simulation.getTemporalSchedule().scheduleRepeating(
            startingDateTime.get(simulation),
            ordering,
            steppableObject,
            interval.get(simulation)
        );
        return steppableObject;
    }
}
