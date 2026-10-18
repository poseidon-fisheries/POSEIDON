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

package uk.ac.ox.poseidon.agents.vessels;

import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.geography.ports.Port;

/**
 * A {@link SimulationScopeFactory} counterpart of
 * {@link CreateVesselsAtPort}, built via
 * {@link Factories#createVesselsAtPort}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class CreateVesselsAtPortFactory
  extends SimulationScopeFactory<CreateVesselsAtPort>
{

  private final Factory<
    ? super SimulationScope,
    ? extends VesselSpecification
  > vesselSpecification;
  private final Factory<
    ? super SimulationScope,
    ? extends Supplier<String>
  > vesselIds;
  private final Factory<? super SimulationScope, ? extends Port> port;
  private final int numberOfVessels;

  @Override
  protected CreateVesselsAtPort newInstance(final SimulationScope scope) {
    return new CreateVesselsAtPort(
      vesselSpecification.get(scope),
      vesselIds.get(scope),
      port.get(scope),
      numberOfVessels
    );
  }
}
