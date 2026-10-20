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

import static com.google.common.base.Preconditions.checkArgument;
import static uk.ac.ox.poseidon.core.events.Factories.eventManager;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.currentDateTime;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.startingDateTime;

import com.badlogic.gdx.ai.btree.Task;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import java.util.function.Supplier;
import sim.util.Int2D;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.events.Listener;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.market.MarketGrid;
import uk.ac.ox.poseidon.fishing.tasks.TaskFactory;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * Factories for a vessel's scope, its fleet, what it is made of, and what
 * creates it and states things about it.
 */
@SuppressWarnings("varargs")
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
   * @param attributes the attributes the choice reads, in the order each row
   *                   gives their values; from {@link #by}
   * @param entries    the rows, from {@link #when}, and at most one default,
   *                   from {@link #otherwise}
   * @return a vessel component chosen by {@code attributes}
   * @throws IllegalArgumentException if there are two defaults, a row does not
   *                                  give one value for each attribute, or two
   *                                  rows give the same values
   * @see ChosenComponent
   */
  @SafeVarargs
  public static <T> ChosenComponent<T> choice(
    final ImmutableList<String> attributes,
    final ChoiceEntry<? extends T>... entries
  ) {
    final ChosenComponent.ChosenComponentBuilder<T> choice =
      ChosenComponent.<T>builder().attributes(attributes);
    ChoiceOutcome<? extends T> defaultOutcome = null;
    for (final ChoiceEntry<? extends T> entry : entries) {
      switch (entry) {
        case final ChoiceRow<? extends T> row -> choice.row(row);
        case final ChoiceDefault<? extends T> choiceDefault -> {
          checkArgument(
            defaultOutcome == null,
            "The choice by %s has two defaults",
            attributes
          );
          defaultOutcome = choiceDefault.outcome();
        }
      }
    }
    return choice.defaultOutcome(defaultOutcome).build();
  }

  /**
   * @param attributes the attributes a choice reads
   * @return {@code attributes}, in their order, for {@link #choice}
   */
  public static ImmutableList<String> by(final String... attributes) {
    return ImmutableList.copyOf(attributes);
  }

  /**
   * @param values exact values of the attributes a choice reads, in their
   *               order
   * @return the start of a row of the choice for these values, to which
   *         {@link ChoiceCondition#then} gives what they give
   */
  public static ChoiceCondition when(final Object... values) {
    return new ChoiceCondition(ImmutableList.copyOf(values));
  }

  /**
   * @param factory builds the component for a vessel whose values no row
   *                lists
   * @return the default of a choice, giving what {@code factory} builds
   */
  public static <T> ChoiceDefault<T> otherwise(
    final Factory<? super VesselComponentScope, ? extends T> factory
  ) {
    return new ChoiceDefault<>(new BuildOutcome<>(factory));
  }

  /**
   * @param noComponent {@link #none}
   * @return the default of a choice, giving a vessel whose values no row
   *         lists no component
   */
  public static <T> ChoiceDefault<T> otherwise(
    final NoComponent<T> noComponent
  ) {
    return new ChoiceDefault<>(noComponent);
  }

  /**
   * @return the outcome of a choice that gives no component, for what the
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
      currentDateTime(),
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
