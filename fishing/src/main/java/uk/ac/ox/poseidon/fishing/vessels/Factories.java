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
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.currentDateTime;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.startingDateTime;

import com.badlogic.gdx.ai.btree.Task;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
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
import uk.ac.ox.poseidon.fishing.market.MarketGrid;
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
   * The specification is built by the returned builder, given the vessel's
   * other components with {@code component(name, component)}, in the order
   * they are built, and the attributes that give a vessel's home port, with
   * {@code homePortAttribute(name)}, and, if vessels have names, its name,
   * with {@code nameAttribute(name)}.
   *
   * @param fleet     the fleet the vessels join
   * @param behaviour the component that is the behaviour each vessel steps
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
   * The register's columns and event codes are those of the EU fleet register:
   * the vessel id in {@code cfr}, the event code in {@code event}, the event
   * date in {@code event_start_date}, with {@code event_end_date} left out of
   * the attributes; CEN, CST, IMP and CHA create a vessel, MOD changes it, and
   * DES, EXP and RET make it exit. Change them on the factory's builder.
   *
   * @param data                the vessel register table
   * @param vesselSpecification what the vessels are made of
   * @return a {@link SimulationScopeFactory} for the register's statements,
   *         dated; pass it to
   *         {@link uk.ac.ox.poseidon.core.schedule.Factories#scheduledByDateTime(Factory)}
   * @see VesselRegisterFactory
   */
  public static VesselRegisterFactory vesselRegister(
    final Factory<? super SimulationScope, ? extends Table> data,
    final Factory<
      ? super SimulationScope,
      ? extends VesselSpecification
    > vesselSpecification
  ) {
    return new VesselRegisterFactory(
      data,
      vesselSpecification,
      startingDateTime(),
      "cfr",
      "event",
      "event_start_date",
      ImmutableList.of("event_end_date"),
      ImmutableList.of("CEN", "CST", "IMP", "CHA"),
      ImmutableList.of("MOD"),
      ImmutableList.of("DES", "EXP", "RET")
    );
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
   * @param attribute the vessel attribute that determines the variant
   * @param rows the listed values and their variants, from {@link #when}
   * @return a component varying by {@code attribute}, without a default
   * @see VaryingComponent
   */
  @SafeVarargs
  public static <T> VaryingComponent<T> varyingBy(
    final String attribute,
    final Map.Entry<Object, ? extends Variant<? extends T>>... rows
  ) {
    return VaryingComponent.<T>builder()
      .attribute(attribute)
      .variants(
        ImmutableMap.<Object, Variant<? extends T>>builder()
          .putAll(Arrays.asList(rows))
          .buildOrThrow()
      )
      .build();
  }

  /**
   * @param attribute the vessel attribute that determines the variant
   * @param factory builds the component for unlisted values
   * @param rows the listed values and their variants, from {@link #when}
   * @return a component varying by {@code attribute}, with a factory default
   * @see VaryingComponent
   */
  @SafeVarargs
  public static <T> VaryingComponent<T> varyingBy(
    final String attribute,
    final Factory<? super VesselComponentScope, ? extends T> factory,
    final Map.Entry<Object, ? extends Variant<? extends T>>... rows
  ) {
    return varyingBy(attribute, rows)
      .toBuilder()
      .defaultVariant(new FactoryVariant<>(factory))
      .build();
  }

  /**
   * @param attribute the vessel attribute that determines the variant
   * @param noComponent {@link #none}
   * @param rows the listed values and their variants, from {@link #when}
   * @return a component varying by {@code attribute}, absent for unlisted values
   * @see VaryingComponent
   */
  @SafeVarargs
  public static <T> VaryingComponent<T> varyingBy(
    final String attribute,
    final NoComponent<T> noComponent,
    final Map.Entry<Object, ? extends Variant<? extends T>>... rows
  ) {
    return varyingBy(attribute, rows)
      .toBuilder()
      .defaultVariant(noComponent)
      .build();
  }

  /**
   * @param value the attribute value
   * @param factory builds the component for this value
   * @return the value paired with its factory variant
   * @see VaryingComponent
   */
  public static <T> Map.Entry<Object, FactoryVariant<T>> when(
    final Object value,
    final Factory<? super VesselComponentScope, ? extends T> factory
  ) {
    return Map.entry(value, new FactoryVariant<>(factory));
  }

  /**
   * @param value the attribute value
   * @param noComponent {@link #none}
   * @return the value paired with deliberate absence
   * @see VaryingComponent
   */
  public static <T> Map.Entry<Object, NoComponent<T>> when(
    final Object value,
    final NoComponent<T> noComponent
  ) {
    return Map.entry(value, noComponent);
  }

  /**
   * @return a variant that gives no component, for what the
   *         scenario does not model
   */
  public static <T> NoComponent<T> none() {
    return new NoComponent<>();
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
      currentDateTime(),
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
      currentDateTime(),
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
      currentDateTime(),
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
      currentDateTime(),
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
