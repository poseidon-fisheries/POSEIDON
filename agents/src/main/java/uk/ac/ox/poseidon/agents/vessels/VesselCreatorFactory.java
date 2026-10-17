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

import static com.google.common.base.Preconditions.checkNotNull;

import com.badlogic.gdx.ai.btree.Task;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.vessels.accounts.Account;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * A {@link SimulationScopeFactory} counterpart of {@link VesselCreator}, built
 * via {@link Factories#vesselCreator}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class VesselCreatorFactory
  extends SimulationScopeFactory<VesselCreator>
  implements CreatesVesselsWithComponents
{

  private final Factory<
    ? super SimulationScope,
    ? extends VesselField
  > vesselField;
  private final Factory<? super SimulationScope, ? extends PortGrid> portGrid;
  private final Factory<
    ? super SimulationScope,
    ? extends MarketGrid
  > marketGrid;
  private final Factory<
    ? super SimulationScope,
    ? extends Supplier<String>
  > vesselIdSupplier;

  private final ValueFactory<? super VesselScope, ? super Vessel, String> name;
  private final Factory<? super VesselScope, ? extends Account> account;
  private final Factory<? super VesselScope, ? extends Port> homePort;
  private final VesselComponent<? extends Task<Vessel>> behaviour;

  @Singular
  private final ImmutableList<VesselComponent<?>> components;

  private final int numberOfVesselsToCreate;
  private final Factory<
    ? super SimulationScope,
    ? extends EventManager
  > eventManager;

  /** @return the behaviour's declaration, then the other components' */
  @Override
  public Stream<VesselComponent<?>> listedComponents() {
    return Stream.concat(
      Stream.of(behaviour),
      components == null ? Stream.empty() : components.stream()
    );
  }

  /**
   * @return a {@link VesselCreator} ready to create
   *         {@link #numberOfVesselsToCreate} vessels
   */
  @Override
  protected VesselCreator newInstance(final SimulationScope scope) {
    return new VesselCreator(
      eventManager.get(scope),
      checkNotNull(vesselField).get(scope),
      checkNotNull(portGrid).get(scope),
      checkNotNull(marketGrid).get(scope),
      checkNotNull(vesselIdSupplier).get(scope),
      checkNotNull(name),
      checkNotNull(account),
      checkNotNull(homePort),
      checkNotNull(behaviour),
      components == null ? List.of() : components,
      numberOfVesselsToCreate
    );
  }
}
