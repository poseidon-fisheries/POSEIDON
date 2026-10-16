/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

package uk.ac.ox.poseidon.core;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.providers.temporal.CurrentDateTime;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

/**
 * A {@link SimulationScopeFactory} for a {@link CurrentDateTime}: the
 * simulation's clock, which factories take as an input rather than reading
 * the simulation's schedule. Built via
 * {@link uk.ac.ox.poseidon.core.providers.temporal.Factories#currentDateTime()}.
 */
@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class CurrentDateTimeFactory
  extends SimulationScopeFactory<CurrentDateTime>
{

  @Override
  protected CurrentDateTime newInstance(final SimulationScope scope) {
    return new CurrentDateTime(scope.getSimulation().getTemporalSchedule());
  }
}
