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

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.tasks.Behaviour;
import uk.ac.ox.poseidon.agents.vessels.accounts.Account;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
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

  private final Factory<? super VesselScope, ? extends String> name;
  private final Factory<? super VesselScope, ? extends Account> account;
  private final Factory<? super VesselScope, ? extends Port> homePort;
  private final Factory<? super VesselScope, ? extends Hold> hold;
  private final Factory<? super VesselScope, ? extends Gear> gear;
  private final Factory<? super VesselScope, ? extends Engine> engine;
  private final Factory<? super VesselScope, ? extends Behaviour> behaviour;

  @Singular
  private final ImmutableList<Factory<? super VesselScope, ?>> extraFactories;

  private final int numberOfVesselsToCreate;
  private final Factory<
    ? super SimulationScope,
    ? extends EventManager
  > eventManager;

  /** Shorthand for the full constructor with no extra per-vessel factories. */
  public VesselCreatorFactory(
    final Factory<? super SimulationScope, ? extends VesselField> vesselField,
    final Factory<? super SimulationScope, ? extends PortGrid> portGrid,
    final Factory<? super SimulationScope, ? extends MarketGrid> marketGrid,
    final Factory<
      ? super SimulationScope,
      ? extends Supplier<String>
    > vesselIdSupplier,
    final Factory<? super VesselScope, ? extends String> name,
    final Factory<? super VesselScope, ? extends Account> account,
    final Factory<? super VesselScope, ? extends Port> homePort,
    final Factory<? super VesselScope, ? extends Hold> hold,
    final Factory<? super VesselScope, ? extends Gear> gear,
    final Factory<? super VesselScope, ? extends Engine> engine,
    final Factory<? super VesselScope, ? extends Behaviour> behaviour,
    final int numberOfVesselsToCreate,
    final Factory<? super SimulationScope, ? extends EventManager> eventManager
  ) {
    this(
      vesselField,
      portGrid,
      marketGrid,
      vesselIdSupplier,
      name,
      account,
      homePort,
      hold,
      gear,
      engine,
      behaviour,
      ImmutableList.of(),
      numberOfVesselsToCreate,
      eventManager
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
      checkNotNull(hold),
      checkNotNull(gear),
      checkNotNull(engine),
      checkNotNull(behaviour),
      extraFactories == null ? List.of() : extraFactories,
      numberOfVesselsToCreate
    );
  }
}
