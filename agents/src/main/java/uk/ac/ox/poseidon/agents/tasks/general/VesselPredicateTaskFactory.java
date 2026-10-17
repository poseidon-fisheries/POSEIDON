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

package uk.ac.ox.poseidon.agents.tasks.general;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.agents.tasks.TaskFactory;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * A {@link TaskFactory} counterpart of {@link AgentPredicateTask}, built
 * via {@link Factories Factories.checkThat(...)}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class VesselPredicateTaskFactory
  extends TaskFactory<AgentPredicateTask<Vessel>>
{

  private final ValueFactory<
    ? super VesselComponentScope,
    ? super Vessel,
    Boolean
  > predicate;

  @Override
  protected AgentPredicateTask<Vessel> newInstance(
    final VesselComponentScope scope
  ) {
    return new AgentPredicateTask<>(predicate.get(scope));
  }
}
