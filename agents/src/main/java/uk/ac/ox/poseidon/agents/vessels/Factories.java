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

import static uk.ac.ox.poseidon.core.events.Factories.eventManager;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Supplier;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.tasks.Behaviour;
import uk.ac.ox.poseidon.agents.vessels.accounts.Account;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.events.Listener;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * Factories for a vessel's scope, its fleet, and the events that create/update
 * vessels.
 */
public class Factories {

  private Factories() {}

  /**
   * @param delegate factory for the value to ask once per vessel, with no
   *                 context
   * @return a {@link ValueFactory} for a constant of the delegate's first value
   *         for each vessel
   * @see FirstValueFromFactory
   */
  public static <R> FirstValueFromFactory<R> firstValueFrom(
    final ValueFactory<? super VesselScope, ? super Void, ? extends R> delegate
  ) {
    return new FirstValueFromFactory<>(delegate);
  }

  /**
   * @param vesselField the field vessels move within
   * @param portGrid    the fleet's ports
   * @param marketGrid  the fleet's markets
   * @return a {@link SimulationScopeFactory} for a {@link Fleet}
   * @see Fleet
   */
  public static FleetFactory fleet(
    final Factory<? super SimulationScope, ? extends VesselField> vesselField,
    final Factory<? super SimulationScope, ? extends PortGrid> portGrid,
    final Factory<? super SimulationScope, ? extends MarketGrid> marketGrid
  ) {
    return new FleetFactory(vesselField, portGrid, marketGrid, eventManager());
  }

  /**
   * @param prefix the prefix to prepend to the vessel's own id
   * @return a {@link ValueFactory} for a simple vessel name, read off the
   *         vessel it is given
   * @see PrefixedIdFactory
   */
  public static PrefixedIdFactory prefixedId(final String prefix) {
    return new PrefixedIdFactory(prefix);
  }

  /**
   * @param vesselField             the field vessels move within
   * @param portGrid                the vessels' ports
   * @param marketGrid              the vessels' markets
   * @param vesselIdSupplier        supplies each new vessel's id
   * @param name                    reads each vessel's name off it
   * @param account                 builds each vessel's account
   * @param homePort                builds each vessel's home port
   * @param behaviour               declares each vessel's behaviour
   * @param components              declares each vessel's other components,
   *                                in the order they are built
   * @param numberOfVesselsToCreate how many vessels to create
   * @return a {@link SimulationScopeFactory} for a {@link VesselCreator}
   * @see VesselCreator
   */
  public static VesselCreatorFactory vesselCreator(
    final Factory<? super SimulationScope, ? extends VesselField> vesselField,
    final Factory<? super SimulationScope, ? extends PortGrid> portGrid,
    final Factory<? super SimulationScope, ? extends MarketGrid> marketGrid,
    final Factory<
      ? super SimulationScope,
      ? extends Supplier<String>
    > vesselIdSupplier,
    final ValueFactory<? super VesselScope, ? super Vessel, String> name,
    final Factory<? super VesselScope, ? extends Account> account,
    final Factory<? super VesselScope, ? extends Port> homePort,
    final VesselComponent<? extends Behaviour> behaviour,
    final List<VesselComponent<?>> components,
    final int numberOfVesselsToCreate
  ) {
    return new VesselCreatorFactory(
      vesselField,
      portGrid,
      marketGrid,
      vesselIdSupplier,
      name,
      account,
      homePort,
      behaviour,
      ImmutableList.copyOf(components),
      numberOfVesselsToCreate,
      eventManager()
    );
  }

  /**
   * @param factory builds the component, for each vessel
   * @return the declaration of a vessel component built by {@code factory}
   * @see VesselComponent
   */
  public static <T> VesselComponent<T> vesselComponent(
    final Factory<? super VesselComponentScope, ? extends T> factory
  ) {
    return new VesselComponent<>(factory);
  }

  /**
   * @param listener the factory giving the vessel's listener
   * @return a {@link VesselScopeFactory} that registers the vessel's listener
   *         on the vessel's event manager and gives it; declare it as one of
   *         the vessel's components
   * @see VesselEventListenerFactory
   */
  public static <L extends Listener<?>> VesselEventListenerFactory<L> vesselEventListener(
    final Factory<? super VesselScope, ? extends L> listener
  ) {
    return new VesselEventListenerFactory<>(listener);
  }
}
