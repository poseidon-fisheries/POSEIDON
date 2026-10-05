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

package uk.ac.ox.poseidon.core.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sim.engine.Steppable;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map.Entry;

/**
 * A {@link SimulationScopeFactory} that schedules each resolved steppable to run once at its own
 * date-time, via {@link TemporalSchedule#scheduleByDateTime}, and returns those same dated
 * steppables. Steppables dated before the simulation's start all run at the start, in date order.
 * No separate plain component class here: scheduling is a side effect of building the factory,
 * not a distinct behavior class. Built via {@link Factories#scheduledByDateTime(Factory)}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ScheduledByDateTimeFactory<C extends Steppable>
    extends SimulationScopeFactory<List<Entry<LocalDateTime, C>>> {

    private Factory<? super SimulationScope, ? extends List<Entry<LocalDateTime, C>>>
        steppablesByDateTime;

    @Override
    protected List<Entry<LocalDateTime, C>> newInstance(final SimulationScope scope) {
        final List<Entry<LocalDateTime, C>> steppables = steppablesByDateTime.get(scope);
        scope.getSimulation().getTemporalSchedule().scheduleByDateTime(steppables);
        return steppables;
    }
}
