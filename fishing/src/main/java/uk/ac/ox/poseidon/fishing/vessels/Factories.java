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

package uk.ac.ox.poseidon.fishing.vessels;

import static uk.ac.ox.poseidon.core.events.Factories.eventManager;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.startingDateTime;

import com.badlogic.gdx.ai.btree.Task;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.joda.money.Money;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.events.Listener;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.tasks.TaskFactory;
import uk.ac.ox.poseidon.geography.Destination;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * Factories for a vessel's scope, its fleet, what it is made of, and what
 * creates it and states things about it.
 */
@SuppressWarnings({ "overloads", "varargs" })
public final class Factories {

  private Factories() {}

  /**
   * @param delegate factory for the value to ask once per vessel component,
   *                 with no context
   * @return a {@link ValueFactory} for a constant of one value of the delegate,
   *         for each vessel component
   * @see OneValueFromFactory
   */
  public static <R> OneValueFromFactory<R> oneValueFrom(
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Void,
      ? extends R
    > delegate
  ) {
    return new OneValueFromFactory<>(delegate);
  }

  /**
   * @param vesselField the field vessels move within
   * @param portGrid    the fleet's ports
   * @return a {@link SimulationScopeFactory} for a {@link Fleet}
   * @see Fleet
   */
  public static FleetFactory fleet(
    final Factory<? super SimulationScope, ? extends VesselField> vesselField,
    final Factory<? super SimulationScope, ? extends PortGrid> portGrid
  ) {
    return new FleetFactory(vesselField, portGrid, eventManager());
  }

  /**
   * @param fleet      the fleet the vessels join
   * @param behaviour  the component that is the behaviour each vessel steps
   * @param components the vessel's other components as name and component
   *                   entries, in the order they are built
   * @return a {@link SimulationScopeFactory} for a
   *         {@link VesselSpecification}
   * @throws IllegalArgumentException if a component name is given twice
   * @see VesselSpecification
   */
  @SafeVarargs
  public static VesselSpecificationFactory vesselSpecification(
    final Factory<? super SimulationScope, ? extends Fleet> fleet,
    final VesselComponent<? extends Task<Vessel>> behaviour,
    final Map.Entry<String, ? extends VesselComponent<?>>... components
  ) {
    return VesselSpecificationFactory.builder()
      .fleet(fleet)
      .behaviour(behaviour)
      .components(
        ImmutableMap.<String, VesselComponent<?>>builder()
          .putAll(Arrays.asList(components))
          .buildOrThrow()
      )
      .build();
  }

  /**
   * @param vesselSpecification what the vessels are made of
   * @param vesselIds           gives each new vessel's id, from the port it
   *                            is created at; called once per vessel
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
    final MappingFactory<
      ? super SimulationScope,
      ? super Void,
      ? super Port,
      ? extends String
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
   * The register's table is read through the named columns and codes and nothing
   * else: only the listed attribute columns become vessel
   * attributes. A row whose event code is a creation code creates the vessel
   * with its attributes and home-port code. A change code gives the vessel what
   * differs from its previous row: an attribute update for changed attributes
   * and a home-port update for a changed code, a missing value counting as a
   * change, and nothing for what is unchanged. An exit code makes the vessel
   * exit. Rows dated at or before the start fold into the vessel's state at the
   * start. At simulation startup a named column missing from the table, or a
   * row whose event code is in none of the lists, is rejected, naming it.
   * There is no default format.
   *
   * @param data                the vessel register table
   * @param vesselSpecification what the vessels are made of
   * @param vesselIdColumn      the column giving each row's vessel id
   * @param eventCodeColumn     the column giving each row's event code
   * @param eventDateColumn     the column giving each row's event date
   * @param homePortCodeColumn  the column giving each row's home-port code
   * @param attributeColumns    the columns that become vessel attributes; only
   *                            these do, a role column among them included
   * @param creationCodes       the codes of rows that create a vessel; not empty
   * @param changeCodes         the codes of rows that change a vessel
   * @param exitCodes           the codes of rows that make a vessel exit
   * @return a {@link SimulationScopeFactory} for the register's statements,
   *         dated; pass it to
   *         {@link uk.ac.ox.poseidon.core.schedule.Factories#scheduledByDateTime(Factory)}
   * @throws IllegalArgumentException if there is no creation code, or a code is
   *                                  in two lists
   * @see VesselRegisterFactory
   */
  public static VesselRegisterFactory vesselRegister(
    final Factory<? super SimulationScope, ? extends Table> data,
    final Factory<
      ? super SimulationScope,
      ? extends VesselSpecification
    > vesselSpecification,
    final String vesselIdColumn,
    final String eventCodeColumn,
    final String eventDateColumn,
    final String homePortCodeColumn,
    final List<String> attributeColumns,
    final List<String> creationCodes,
    final List<String> changeCodes,
    final List<String> exitCodes
  ) {
    return VesselRegisterFactory.builder()
      .data(data)
      .vesselSpecification(vesselSpecification)
      .startingDateTime(startingDateTime())
      .vesselIdColumn(vesselIdColumn)
      .eventCodeColumn(eventCodeColumn)
      .eventDateColumn(eventDateColumn)
      .homePortCodeColumn(homePortCodeColumn)
      .attributeColumns(attributeColumns)
      .creationCodes(creationCodes)
      .changeCodes(changeCodes)
      .exitCodes(exitCodes)
      .build();
  }

  /**
   * @param factory builds the component, for each vessel
   * @return a vessel component built by {@code factory}
   * @see FactoryComponent
   */
  public static <T> FactoryComponent<T> vesselComponent(
    final Factory<? super VesselComponentScope, ? extends T> factory
  ) {
    return new FactoryComponent<>(factory);
  }

  /**
   * @param attribute the vessel attribute that determines the factory
   * @param entries   the listed values, which are strings, and the factory that
   *                  builds the component for each, in the order given
   * @return a component varying by {@code attribute}
   * @throws IllegalArgumentException if a value is listed twice
   * @see VaryingComponent
   */
  @SafeVarargs
  public static <T> VaryingComponent<T> varyingBy(
    final String attribute,
    final Map.Entry<
      String,
      ? extends Factory<? super VesselComponentScope, ? extends T>
    >... entries
  ) {
    return VaryingComponent.<T>builder()
      .attribute(attribute)
      .factories(
        ImmutableMap.<
          String,
          Factory<? super VesselComponentScope, ? extends T>
        >builder()
          .putAll(Arrays.asList(entries))
          .buildOrThrow()
      )
      .build();
  }

  /**
   * @param rootTask builds the root task of the vessel's behaviour tree, for
   *                 each vessel
   * @return the component that is the behaviour the vessel runs, built by
   *         {@code rootTask}; pass it where a specification takes a vessel's
   *         behaviour
   * @see FactoryComponent
   */
  public static FactoryComponent<Task<Vessel>> behaviour(
    final Factory<? super VesselComponentScope, ? extends Task<Vessel>> rootTask
  ) {
    return new FactoryComponent<>(rootTask);
  }

  /**
   * @param initialDestination gives the trip's first destination for the
   *                           vessel, or none if it should not leave; for a
   *                           destination that always exists, such as the
   *                           vessel's home port, wrap it in
   *                           {@link uk.ac.ox.poseidon.core.functions.Factories#some
   *                           some(...)}
   * @param children           the tasks run in sequence during the trip
   * @return a {@link TaskFactory} for a {@link TripBlock}, which opens a trip,
   *         runs {@code children} and closes the trip, charging no cost
   * @see TripBlockFactory
   */
  @SafeVarargs
  public static TripBlockFactory trip(
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      ? extends Optional<? extends Destination>
    > initialDestination,
    final Factory<
      ? super VesselComponentScope,
      ? extends Task<Vessel>
    >... children
  ) {
    return new TripBlockFactory(
      initialDestination,
      ImmutableList.of(),
      ImmutableList.copyOf(children)
    );
  }

  /**
   * @param initialDestination gives the trip's first destination for the
   *                           vessel, or none if it should not leave; for a
   *                           destination that always exists, such as the
   *                           vessel's home port, wrap it in
   *                           {@link uk.ac.ox.poseidon.core.functions.Factories#some
   *                           some(...)}
   * @param tripCost           gives the cost of the finished trip, charged as
   *                           the block closes it
   * @param children           the tasks run in sequence during the trip
   * @return a {@link TaskFactory} for a {@link TripBlock}, which opens a trip,
   *         runs {@code children} and closes the trip, charging
   *         {@code tripCost}
   * @see TripBlockFactory
   */
  @SafeVarargs
  public static TripBlockFactory trip(
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      ? extends Optional<? extends Destination>
    > initialDestination,
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Trip,
      ? extends Money
    > tripCost,
    final Factory<
      ? super VesselComponentScope,
      ? extends Task<Vessel>
    >... children
  ) {
    return new TripBlockFactory(
      initialDestination,
      ImmutableList.of(tripCost),
      ImmutableList.copyOf(children)
    );
  }

  /**
   * @param initialDestination gives the trip's first destination for the
   *                           vessel, or none if it should not leave; for a
   *                           destination that always exists, such as the
   *                           vessel's home port, wrap it in
   *                           {@link uk.ac.ox.poseidon.core.functions.Factories#some
   *                           some(...)}
   * @param children           the tasks run in sequence during the trip
   * @return a {@link TaskFactory} for a {@link FishingTripBlock}, which opens a
   *         fishing trip, runs {@code children} and closes the trip, charging
   *         no cost
   * @see FishingTripBlockFactory
   */
  @SafeVarargs
  public static FishingTripBlockFactory fishingTrip(
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      ? extends Optional<? extends Destination>
    > initialDestination,
    final Factory<
      ? super VesselComponentScope,
      ? extends Task<Vessel>
    >... children
  ) {
    return new FishingTripBlockFactory(
      initialDestination,
      ImmutableList.of(),
      ImmutableList.copyOf(children)
    );
  }

  /**
   * @param initialDestination gives the trip's first destination for the
   *                           vessel, or none if it should not leave; for a
   *                           destination that always exists, such as the
   *                           vessel's home port, wrap it in
   *                           {@link uk.ac.ox.poseidon.core.functions.Factories#some
   *                           some(...)}
   * @param tripCost           gives the cost of the finished trip, charged as
   *                           the block closes it
   * @param children           the tasks run in sequence during the trip
   * @return a {@link TaskFactory} for a {@link FishingTripBlock}, which opens a
   *         fishing trip, runs {@code children} and closes the trip, charging
   *         {@code tripCost}
   * @see FishingTripBlockFactory
   */
  @SafeVarargs
  public static FishingTripBlockFactory fishingTrip(
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      ? extends Optional<? extends Destination>
    > initialDestination,
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Trip,
      ? extends Money
    > tripCost,
    final Factory<
      ? super VesselComponentScope,
      ? extends Task<Vessel>
    >... children
  ) {
    return new FishingTripBlockFactory(
      initialDestination,
      ImmutableList.of(tripCost),
      ImmutableList.copyOf(children)
    );
  }

  /**
   * @param listener the factory giving the vessel's listener
   * @return a {@link VesselComponentScopeFactory} that registers the vessel's
   *         listener on the vessel's event manager and gives it; make it one
   *         of the vessel's components
   * @see VesselEventListenerFactory
   */
  public static <L extends Listener<?>> VesselEventListenerFactory<L> vesselEventListener(
    final Factory<? super VesselComponentScope, ? extends L> listener
  ) {
    return new VesselEventListenerFactory<>(listener);
  }
}
