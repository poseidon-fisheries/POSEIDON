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

import com.badlogic.gdx.ai.btree.Task;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import java.util.function.Supplier;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.tasks.TaskFactory;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.events.Listener;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * Factories for a vessel's scope, its fleet, what it is made of, and what
 * creates it.
 */
public class Factories {

  private Factories() {}

  /**
   * @param delegate factory for the value to ask once per vessel, with no
   *                 context
   * @return a {@link ValueFactory} for a constant of one value of the delegate,
   *         for each vessel
   * @see OneValueFromFactory
   */
  public static <R> OneValueFromFactory<R> oneValueFrom(
    final ValueFactory<? super VesselScope, ? super Void, ? extends R> delegate
  ) {
    return new OneValueFromFactory<>(delegate);
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
   * The specification is built by the returned builder, given the
   * declarations of the vessel's other components with {@code component(name,
   * declaration)}, in the order they are built, and the attributes that give a
   * vessel's home port, with {@code homePortAttribute(name)}, and, if vessels
   * have names, its name, with {@code nameAttribute(name)}.
   *
   * @param fleet     the fleet the vessels join
   * @param behaviour declares the behaviour each vessel steps
   * @return a builder for a {@link SimulationScopeFactory} for a
   *         {@link VesselSpecification}
   * @see VesselSpecification
   */
  public static VesselSpecificationFactory.VesselSpecificationFactoryBuilder<
    ?,
    ?
  > vesselSpecification(
    final Factory<? super SimulationScope, ? extends Fleet> fleet,
    final VesselComponent<? extends Task<Vessel>> behaviour
  ) {
    return VesselSpecificationFactory.builder()
      .fleet(fleet)
      .behaviour(behaviour);
  }

  /**
   * @param vesselSpecification what the vessels are made of
   * @param vesselIds           supplies each new vessel's id
   * @param port                the vessels' home port
   * @param numberOfVessels     how many vessels to create
   * @return a {@link SimulationScopeFactory} for a
   *         {@link CreateVesselsAtPort}, which creates the vessels when it is
   *         stepped
   * @see CreateVesselsAtPort
   */
  public static CreateVesselsAtPortFactory createVesselsAtPort(
    final Factory<
      ? super SimulationScope,
      ? extends VesselSpecification
    > vesselSpecification,
    final Factory<
      ? super SimulationScope,
      ? extends Supplier<String>
    > vesselIds,
    final Factory<? super SimulationScope, ? extends Port> port,
    final int numberOfVessels
  ) {
    return new CreateVesselsAtPortFactory(
      vesselSpecification,
      vesselIds,
      port,
      numberOfVessels
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
   * @param rootTask builds the root task of the vessel's behaviour tree, for
   *                 each vessel
   * @return the declaration of the behaviour the vessel runs, built by
   *         {@code rootTask}; pass it where a vessel's behaviour is declared
   * @see VesselComponent
   */
  public static VesselComponent<Task<Vessel>> behaviour(
    final Factory<? super VesselComponentScope, ? extends Task<Vessel>> rootTask
  ) {
    return new VesselComponent<>(rootTask);
  }

  /**
   * @param initialDestination gives the trip's first destination for the
   *                           vessel, or none if it should not leave
   * @param children           the tasks run in sequence during the trip
   * @return a {@link TaskFactory} for a {@link TripBlock}, which opens a trip,
   *         runs {@code children} and closes the trip
   * @see TripBlockFactory
   */
  @SafeVarargs
  public static TripBlockFactory trip(
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      Optional<Int2D>
    > initialDestination,
    final Factory<
      ? super VesselComponentScope,
      ? extends Task<Vessel>
    >... children
  ) {
    return new TripBlockFactory(
      initialDestination,
      ImmutableList.copyOf(children)
    );
  }

  /**
   * @param listener the factory giving the vessel's listener
   * @return a {@link VesselComponentScopeFactory} that registers the vessel's
   *         listener on the vessel's event manager and gives it; declare it as
   *         one of the vessel's components
   * @see VesselEventListenerFactory
   */
  public static <L extends Listener<?>> VesselEventListenerFactory<L> vesselEventListener(
    final Factory<? super VesselComponentScope, ? extends L> listener
  ) {
    return new VesselEventListenerFactory<>(listener);
  }
}
