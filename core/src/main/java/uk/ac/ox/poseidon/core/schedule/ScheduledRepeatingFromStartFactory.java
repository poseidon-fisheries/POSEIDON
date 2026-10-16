/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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

import java.time.temporal.TemporalAmount;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import sim.engine.Steppable;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

/**
 * A {@link SimulationScopeFactory} that schedules the resolved steppable to run
 * repeatedly, at a fixed ordering, starting at the simulation's starting
 * date-time and recurring at a resolved interval, and returns that same
 * steppable. No separate plain component class here: scheduling is a side
 * effect of building the factory, not a distinct behavior class. Built via
 * {@link Factories Factories.scheduledRepeatingFromStart(...)}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class ScheduledRepeatingFromStartFactory<C extends Steppable> extends SimulationScopeFactory<C> {

  private final Factory<
    ? super SimulationScope,
    ? extends TemporalAmount
  > interval;
  private final Factory<? super SimulationScope, ? extends C> steppable;
  private final int ordering;

  @Override
  protected C newInstance(final SimulationScope scope) {
    final C steppableObject = steppable.get(scope);
    scope
      .getSimulation()
      .getTemporalSchedule()
      .scheduleRepeating(
        scope.getSimulation().getTemporalSchedule().getStartingDateTime(),
        ordering,
        steppableObject,
        interval.get(scope)
      );
    return steppableObject;
  }
}
