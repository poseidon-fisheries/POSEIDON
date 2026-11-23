/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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

import static com.badlogic.gdx.ai.btree.Task.Status.RUNNING;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static lombok.AccessLevel.NONE;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.google.common.collect.ImmutableMap;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Getter;
import lombok.NonNull;
import org.joda.money.Money;
import sim.portrayal.Oriented2D;
import sim.util.Double2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.vessels.Fleet.StepScheduler;
import uk.ac.ox.poseidon.fishing.vessels.VesselStatement.AttributeUpdate;
import uk.ac.ox.poseidon.fishing.vessels.VesselStatement.Exit;
import uk.ac.ox.poseidon.fishing.vessels.VesselStatement.HomePortUpdate;
import uk.ac.ox.poseidon.fishing.vessels.Whereabouts.Absent;
import uk.ac.ox.poseidon.fishing.vessels.Whereabouts.AtSea;
import uk.ac.ox.poseidon.fishing.vessels.Whereabouts.Docked;
import uk.ac.ox.poseidon.fishing.vessels.accounts.Account;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * A fishing vessel, the model's agent: it has attributes, among them its home
 * port's code and its name, and components (its behaviour, gear, engine, hold
 * and the rest, each read through its {@link VesselComponent}), both as its
 * {@link VesselSpecification} says. Its behaviour is the root
 * task of a gdx-ai behaviour tree, which the vessel runs: it repeatedly steps
 * the tree, rescheduling itself either after the running task's
 * {@link #taskDuration} (while the behaviour is running) or on the very next
 * tick (once it isn't). It steps its behaviour only while it is
 * {@link #isPresent() present}.
 * <p>
 * What a source states about it, an update of its attributes or home port, or
 * its exit, joins
 * its queue of statements and takes effect at the start of its next step in
 * which the behaviour is not running, never during a run of the behaviour and
 * never inside the call that made the statement: so a vessel doesn't exit at
 * sea, and a change takes effect in port. A statement queued on a vessel with
 * no step pending schedules one, at once. Once the queue is applied, every
 * component whose choice reads an attribute that changed is rebuilt, once;
 * the others, and what the vessel's own store holds, stay. An exit ends the
 * queue: the vessel leaves its fleet absent and with no component left,
 * and nothing after it in the queue applies, nor any rebuild. Its current
 * state (cell, heading, whereabouts, trips) is changed by the
 * behaviour itself, at once.
 * <p>
 * A vessel's {@link Whereabouts} are exactly one of absent, docked at a port or
 * at sea, and the vessel field agrees: an absent vessel is off it. A vessel is
 * created absent and placed, silently, docked at its home port if it has one.
 * Docking ({@link #dockAt}) and undocking ({@link #undock}) are explicit acts,
 * each at a time, and the vessel cannot move while docked: a cell can hold
 * several ports, so the port a vessel is docked at is never inferred from its
 * cell. A vessel is present when its home port is a port of the port grid and
 * its behaviour has a factory for its attributes, and absent otherwise, once
 * its statements are applied. An absent vessel is modelled elsewhere, if at
 * all. One that stops
 * resolving while docked becomes absent, and one that resolves again becomes
 * present, docked at its home port. An absent vessel builds nothing: becoming
 * absent drops every component, becoming present builds them all, and a
 * present vessel always has its behaviour. Each change announces itself,
 * once, as a {@link BecameAbsentEvent} or {@link BecamePresentEvent}, after
 * the components are dropped or built.
 * A vessel at sea cannot become absent, or exit.
 * <p>
 * A vessel has at most one step pending on the schedule. It holds no
 * schedule: its fleet gives it a {@link StepScheduler} that schedules its step
 * and nothing else, and refuses to schedule a second one. Nothing outside the
 * vessel can schedule its step. Its first step is at the instant it is
 * created; outside a step, a present vessel always has a step pending. A
 * vessel that becomes absent schedules nothing more, until a statement gives
 * it a step.
 * <p>
 * Nor does it hold a clock: what reads the date takes the clock as an input,
 * and gives the vessel the dates it records, such as those of its trips.
 */
@Getter
public class Vessel implements Oriented2D, HasAttributes {

  /** Puts this vessel's step on the schedule. */
  @Getter(NONE)
  private final @NonNull StepScheduler stepScheduler;

  /**
   * Takes this vessel out of its fleet, which announces the exit, and nothing
   * else: the vessel leaves its fleet without holding it.
   */
  @Getter(NONE)
  private final @NonNull Runnable fleetExit;

  private final @NonNull EventManager eventManager;

  /** The products of the factories built for this vessel. */
  private final ProductStore productStore = new ProductStore();

  /**
   * What this vessel is made of: among others, the component that is the
   * behaviour it runs, the root task of its tree (a vessel without that
   * component does not act).
   */
  @Getter(NONE)
  private final @NonNull VesselSpecification specification;

  /**
   * The tree this vessel runs over its behaviour component, renewed whenever
   * that component is built, between runs of the tree; {@code null} until
   * then. It has this vessel as its object only during a step, so a task
   * reading its vessel between steps fails at once.
   */
  @Getter(NONE)
  private BehaviorTree<Vessel> tree;

  /** The statements about this vessel not yet applied, in order. */
  @Getter(NONE)
  private final Queue<VesselStatement> statements = new LinkedList<>();

  /**
   * Whether this vessel is stepping its behaviour, during which a statement
   * waits for the step to schedule the next one.
   */
  @Getter(NONE)
  private boolean steppingBehaviour;

  /**
   * How long until the running behaviour's task is executed again: set by that
   * task when it reports {@code RUNNING} (see
   * {@link uk.ac.ox.poseidon.fishing.tasks.VesselTask#runFor}), and cleared once
   * this vessel's next step is scheduled, so each running step must set its
   * own.
   */
  @Getter(NONE)
  private Duration taskDuration;

  /** This vessel's identifying id. */
  private final @NonNull String id;
  /** The field this vessel moves within. */
  private final @NonNull VesselField vesselField;
  /** The grid of ports this vessel can dock at. */
  private final @NonNull PortGrid portGrid;

  /**
   * Creates an absent vessel, with no behaviour built yet. It starts stepping
   * once its first step is scheduled ({@link #scheduleFirstStep}).
   *
   * @param newStepScheduler makes, from this vessel's step (which is given
   *                         the date-time it runs at), what schedules that
   *                         step and nothing else
   * @param newFleetExit     makes, from this vessel, what takes it out of its
   *                         fleet and nothing else
   * @param eventManager     the event manager this vessel broadcasts on
   * @param id               the vessel's identifying id
   * @param vesselField      the field the vessel moves within
   * @param portGrid         the grid of ports the vessel can dock at
   * @param specification    what the vessel is made of
   */
  Vessel(
    @NonNull final Function<
      Consumer<LocalDateTime>,
      StepScheduler
    > newStepScheduler,
    @NonNull final Function<Vessel, Runnable> newFleetExit,
    @NonNull final EventManager eventManager,
    @NonNull final String id,
    @NonNull final VesselField vesselField,
    @NonNull final PortGrid portGrid,
    @NonNull final VesselSpecification specification
  ) {
    this.stepScheduler = newStepScheduler.apply(this::step);
    this.fleetExit = newFleetExit.apply(this);
    this.eventManager = eventManager;
    this.id = id;
    this.vesselField = vesselField;
    this.portGrid = portGrid;
    this.specification = specification;
  }

  /**
   * This vessel's attributes, replaced as a whole when they change, so that
   * an action keeping the map in place when it was made keeps the attributes
   * the vessel had then.
   */
  @Getter(NONE)
  private ImmutableMap<String, Object> attributes = ImmutableMap.of();

  /**
   * The port in the port grid whose code the vessel was last given as its home
   * port, or nothing if it has no code or no port has it; looked up when the
   * code is given, not on each read, because it is read on every valuation of
   * a fishing option.
   */
  private Optional<Port> homePort = Optional.empty();

  /**
   * The cell {@link #homePort} is placed on, or nothing if there is no home
   * port; looked up with it, since ports do not move and the cell is read on
   * every route to a fishing option.
   */
  private Optional<Cell> homePortCell = Optional.empty();

  /** This vessel's running balance, which changes at once, from any call. */
  private final Account account = new Account();

  /**
   * The store this vessel keeps for each of its components; a component whose
   * choice gives this vessel nothing has none.
   */
  @Getter(NONE)
  private final Map<VesselComponent<?>, ProductStore> componentStores =
    new LinkedHashMap<>();

  /** What each of this vessel's components built for it, by component. */
  @Getter(NONE)
  private final Map<VesselComponent<?>, Object> components =
    new LinkedHashMap<>();

  // Current state variables
  /** This vessel's current heading, in radians. */
  private double heading;

  /**
   * Where this vessel is: absent, docked at a port, or at sea. It is absent
   * until it is placed at its home port, and agrees with the vessel field,
   * where an absent vessel is not.
   */
  private Whereabouts whereabouts = new Absent();

  /** The vessel's current trip, or {@code null} if it isn't on one. */
  private Trip currentTrip;

  /** @return whether this vessel is in the model: docked or at sea */
  public boolean isPresent() {
    return !(whereabouts instanceof Absent);
  }

  /** @return whether this vessel is at sea */
  public boolean isAtSea() {
    return whereabouts instanceof AtSea;
  }

  /** @return whether this vessel's behaviour tree is running */
  private boolean isBehaviourRunning() {
    return tree != null && tree.getStatus() == RUNNING;
  }

  /**
   * Steps the running task, if the behaviour tree is running. If the tree is
   * then not running, applies the queued statements and, if the vessel is
   * present, starts the tree afresh, in this same step. Then, if the vessel is
   * present, schedules the next step after {@link #taskDuration}.
   *
   * @param dateTime the date-time of the step, which goes only in the events
   *                 this vessel broadcasts
   * @throws IllegalStateException if the vessel is present without a behaviour
   *                               tree, or if the tree, started afresh,
   *                               finishes without starting a task: restarting
   *                               it would never move time on
   */
  private void step(final LocalDateTime dateTime) {
    if (isBehaviourRunning()) {
      stepBehaviour();
    }
    if (!isBehaviourRunning()) {
      applyStatements(dateTime);
      if (isPresent()) {
        checkState(
          tree != null,
          "Vessel %s is present without a behaviour.",
          id
        );
        stepBehaviour();
        checkState(
          isBehaviourRunning(),
          "Vessel %s's behaviour finished without starting a task.",
          this
        );
      }
    }
    if (isPresent()) {
      final Duration delay = checkNotNull(
        taskDuration,
        "Vessel %s's behaviour is running, but no task set a duration this step.",
        this
      );
      taskDuration = null;
      stepScheduler.scheduleNextStep(delay);
    }
  }

  /**
   * Applies the queued statements in order; an exit ends this vessel, and the
   * statements after it are never applied. Once they are applied, the vessel
   * reconciles its whereabouts ({@link #reconcileWhereabouts}). A vessel that
   * became absent drops its components, one that became present builds them,
   * and one that stayed present rebuilds what the changes choose anew. Then it
   * announces the one net change in its whereabouts, if there is one.
   *
   * @throws IllegalStateException if the vessel is at sea and its home port or
   *                               behaviour no longer resolves once the
   *                               statements are applied, or exits at sea
   */
  private void applyStatements(final LocalDateTime dateTime) {
    applyStatementsSince(attributes, whereabouts, dateTime);
  }

  private void applyStatementsSince(
    final Map<String, Object> previousAttributes,
    final Whereabouts previousWhereabouts,
    final LocalDateTime dateTime
  ) {
    switch (statements.poll()) {
      case null -> {
        reconcileWhereabouts();
        final boolean wasPresent = !(previousWhereabouts instanceof Absent);
        if (wasPresent && isPresent()) {
          rebuildVaryingAnew(previousAttributes);
        } else if (wasPresent) {
          dropComponents();
        } else {
          buildComponents();
        }
        announceWhereaboutsChange(previousWhereabouts, whereabouts, dateTime);
      }
      case final AttributeUpdate update -> {
        replaceAttributes(update.applyTo(attributes));
        applyStatementsSince(previousAttributes, previousWhereabouts, dateTime);
      }
      case final HomePortUpdate update -> {
        replaceHomePort(update.getCode());
        applyStatementsSince(previousAttributes, previousWhereabouts, dateTime);
      }
      case final Exit _ -> exit(previousWhereabouts, dateTime);
    }
  }

  /**
   * Places this vessel where its home port and behaviour put it: absent unless
   * its home port is a port of the port grid and its behaviour has a factory
   * for its attributes; docked at its home port if it was absent; and where it
   * is otherwise.
   *
   * @throws IllegalStateException if the vessel is at sea and its home port or
   *                               behaviour does not resolve
   */
  private void reconcileWhereabouts() {
    final boolean homePortAndBehaviourResolve =
      homePort.isPresent() &&
      factoryFor(specification.getBehaviour()).isPresent();
    if (!homePortAndBehaviourResolve) {
      checkState(
        !isAtSea(),
        "Vessel %s's home port or behaviour does not resolve, so it cannot be absent at sea.",
        id
      );
      placeAt(new Absent());
    } else if (!isPresent()) {
      placeAt(new Docked(homePort.get()));
    }
  }

  /**
   * Makes {@code newWhereabouts} this vessel's whereabouts, keeping the vessel
   * field in step: an absent vessel is off it, a vessel that becomes docked is
   * placed on its port's cell.
   */
  private void placeAt(final Whereabouts newWhereabouts) {
    if (!newWhereabouts.equals(whereabouts)) {
      switch (newWhereabouts) {
        case final Absent _ -> vesselField.remove(this);
        case final Docked docked -> vesselField.setCell(
          this,
          portGrid.getCell(docked.getPort())
        );
        case final AtSea _ -> throw new IllegalStateException(
          "Vessel %s has no way to be put at sea.".formatted(id)
        );
      }
      this.whereabouts = newWhereabouts;
    }
  }

  /**
   * Broadcasts the change in whereabouts from {@code before} to
   * {@code after} on this vessel's own event manager, if it is one between
   * docked and absent.
   */
  private void announceWhereaboutsChange(
    final Whereabouts before,
    final Whereabouts after,
    final LocalDateTime dateTime
  ) {
    if (before instanceof final Docked docked && after instanceof Absent) {
      eventManager.broadcast(
        new BecameAbsentEvent(this, docked.getPort(), dateTime)
      );
    } else if (
      before instanceof Absent && after instanceof final Docked docked
    ) {
      eventManager.broadcast(
        new BecamePresentEvent(this, docked.getPort(), dateTime)
      );
    }
  }

  /**
   * Steps the behaviour tree, which reaches this vessel only meanwhile.
   *
   * @throws IllegalStateException if the tree is no longer running but a task
   *                               set a duration
   */
  private void stepBehaviour() {
    tree.setObject(this);
    steppingBehaviour = true;
    try {
      tree.step();
    } finally {
      steppingBehaviour = false;
      tree.setObject(null);
    }
    checkState(
      isBehaviourRunning() || taskDuration == null,
      "Vessel %s's behaviour is not running, but a task set a duration this step.",
      this
    );
  }

  /**
   * Sets how long until the running task is executed again. A vessel runs one
   * task at a time, so only one task may set it in a step.
   *
   * @param taskDuration how long until the running task is executed again
   * @throws IllegalStateException if a task already set it this step
   */
  public void setTaskDuration(final Duration taskDuration) {
    checkState(
      this.taskDuration == null,
      "Vessel %s's task duration was already set this step.",
      this
    );
    this.taskDuration = taskDuration;
  }

  /**
   * Schedules this vessel's first step, at the current instant, after the
   * events already due then: the statements due at that instant, which run
   * before vessel steps, are applied in it.
   */
  void scheduleFirstStep() {
    if (!stepScheduler.isStepPending()) {
      stepScheduler.scheduleNextStep(Duration.ZERO);
    }
  }

  /**
   * Queues {@code statement}, to be applied at the start of this vessel's next
   * step in which the behaviour is not running. If no step is pending, one is
   * scheduled at the current instant; a statement made during this vessel's own
   * step waits for the step to schedule the next one.
   */
  void queue(final VesselStatement statement) {
    statements.add(statement);
    if (!steppingBehaviour && !stepScheduler.isStepPending()) {
      stepScheduler.scheduleNextStep(Duration.ZERO);
    }
  }

  /**
   * Checks this vessel is not at sea; ends its component stores, in the reverse
   * of the order its specification builds them, and drops the components,
   * which are no longer its own, so that it has no behaviour; ends its own
   * store; takes it off the vessel field and makes it absent;
   * broadcasts a {@link BecameAbsentEvent} if it was docked when the statements
   * began; and takes it out of its fleet, which announces the exit. Exiting
   * while absent is valid, and announces nothing.
   *
   * @param previousWhereabouts where this vessel was when the statements began
   * @param dateTime            when the vessel exits
   * @throws IllegalStateException if the vessel is at sea
   */
  private void exit(
    final Whereabouts previousWhereabouts,
    final LocalDateTime dateTime
  ) {
    checkState(!isAtSea(), "Vessel %s cannot exit at sea.", id);
    dropComponents();
    productStore.end();
    placeAt(new Absent());
    announceWhereaboutsChange(previousWhereabouts, whereabouts, dateTime);
    fleetExit.run();
  }

  /**
   * Builds every component this vessel's specification lists, in its order,
   * if the vessel is present: the behaviour first, then the others as listed.
   * An absent vessel builds nothing.
   */
  void buildComponents() {
    if (isPresent()) {
      specification.listedComponents().forEach(this::buildComponent);
    }
  }

  /**
   * Ends the stores of every component this vessel has and drops what they
   * built, in the reverse of the order its specification builds them, so that
   * it has no behaviour.
   */
  private void dropComponents() {
    specification
      .listedComponents()
      .toList()
      .reversed()
      .forEach(this::dropComponent);
    tree = null;
  }

  /**
   * Builds what {@code component} gives this vessel, under a scope carrying a
   * fresh store, which this vessel keeps for it. Renews the tree if that is the
   * behaviour. This vessel must be present, its behaviour must not be running,
   * and it must have nothing built for {@code component}.
   *
   * @throws IllegalStateException if {@code component} varies by an attribute
   *                               whose value for this vessel it does not list
   */
  private <T> void buildComponent(final VesselComponent<T> component) {
    final var factory = factoryFor(component).orElseThrow(() -> {
      final String attribute = ((VaryingComponent<T>) component).getAttribute();
      return new IllegalStateException(
        "Vessel %s has the value %s for %s, which the varying component does not list".formatted(
          id,
          attributes.get(attribute),
          attribute
        )
      );
    });
    final ProductStore store = new ProductStore();
    componentStores.put(component, store);
    components.put(
      component,
      factory.get(new VesselComponentScope(specification.scopeOf(this), store))
    );
    if (component == specification.getBehaviour()) {
      tree = new BehaviorTree<>(specification.getBehaviour().of(this));
    }
  }

  /**
   * @return what builds {@code component} for this vessel: its single
   *         factory, or the factory selected by this vessel's attribute;
   *         nothing if the attribute's value is not listed
   */
  private <T> Optional<
    Factory<? super VesselComponentScope, ? extends T>
  > factoryFor(final VesselComponent<T> component) {
    return switch (component) {
      case final FactoryComponent<T> single -> Optional.of(single.getFactory());
      case final VaryingComponent<T> chosen -> chosen.factoryFor(this);
    };
  }

  /**
   * Gives this vessel anew every component that varies by an attribute
   * whose value differs from {@code previousAttributes}, even if the same
   * factory is picked: ends their stores in the reverse of the order its
   * specification builds them, then builds them in that order.
   */
  private void rebuildVaryingAnew(
    final Map<String, Object> previousAttributes
  ) {
    final List<VesselComponent<?>> componentsToRebuild = specification
      .listedComponents()
      .filter(
        component ->
          component instanceof final VaryingComponent<?> chosen &&
          !Objects.equals(
            previousAttributes.get(chosen.getAttribute()),
            attributes.get(chosen.getAttribute())
          )
      )
      .toList();
    componentsToRebuild.reversed().forEach(this::dropComponent);
    componentsToRebuild.forEach(this::buildComponent);
  }

  /**
   * Ends the store this vessel keeps for {@code component}, if it has one,
   * and drops what the component built.
   */
  private void dropComponent(final VesselComponent<?> component) {
    final ProductStore store = componentStores.remove(component);
    if (store != null) store.end();
    components.remove(component);
  }

  /**
   * @param component the component to read
   * @return what {@code component} built for this vessel, if anything
   * @throws IllegalStateException if this vessel was not created with
   *                               {@code component}: a wiring error, not an
   *                               absence
   */
  // The vessel keeps under a component only what its factory built, a T
  @SuppressWarnings("unchecked")
  <T> Optional<T> findComponent(final VesselComponent<T> component) {
    final T product = (T) components.get(component);
    if (product == null) {
      checkState(
        specification
          .listedComponents()
          .anyMatch(listed -> listed == component),
        "Vessel %s was not created with the component %s",
        id,
        component
      );
    }
    return Optional.ofNullable(product);
  }

  /** @return {@link #heading} */
  @Override
  public double orientation2D() {
    return heading;
  }

  /** Points the vessel's heading towards {@code destinationCell}'s centre. */
  public void setHeadingTowards(final Cell destinationCell) {
    setHeadingTowards(getVesselField().getModelGrid().toPoint(destinationCell));
  }

  private void setHeadingTowards(final Double2D destinationPoint) {
    final Double2D location = getPoint();
    final double dx = destinationPoint.x - location.x;
    final double dy = destinationPoint.y - location.y;
    this.heading = Math.atan2(dy, dx);
  }

  /**
   * @return the vessel's continuous-space position
   * @throws IllegalStateException if the vessel is absent
   */
  public Double2D getPoint() {
    checkPresent();
    return vesselField.getPoint(this);
  }

  /**
   * @return the vessel's position, converted to a geographic coordinate
   * @throws IllegalStateException if the vessel is absent
   */
  public Coordinate getCoordinate() {
    return vesselField.getModelGrid().toCoordinate(getPoint());
  }

  /**
   * @return the grid cell the vessel currently occupies
   * @throws IllegalStateException if the vessel is absent
   */
  public Cell getCell() {
    checkPresent();
    return vesselField.getCell(this);
  }

  private void checkPresent() {
    checkState(isPresent(), "Vessel %s is absent, so has no position.", id);
  }

  /**
   * Moves the vessel to {@code cell}.
   *
   * @throws IllegalStateException if the vessel is not at sea
   */
  public void setCurrentCell(final Cell cell) {
    checkState(isAtSea(), "Vessel %s cannot move while %s.", id, whereabouts);
    vesselField.setCell(this, cell);
  }

  /** @return whether this vessel is docked at a port */
  public boolean isDocked() {
    return whereabouts instanceof Docked;
  }

  /**
   * @return the port this vessel is docked at
   * @throws IllegalStateException if the vessel is not docked
   */
  public Port getPortDockedAt() {
    checkState(isDocked(), "Vessel %s is not docked.", id);
    return ((Docked) whereabouts).getPort();
  }

  /** @return whether this vessel is docked at {@code port} */
  public boolean isDockedAt(final Port port) {
    return whereabouts.equals(new Docked(port));
  }

  /**
   * Docks this vessel at {@code port} and broadcasts a {@link DockingEvent}.
   *
   * @param port     the port to dock at
   * @param dateTime when the vessel docks
   * @throws IllegalStateException if the vessel is not at sea, or is not in
   *                               {@code port}'s cell
   */
  public void dockAt(final Port port, final LocalDateTime dateTime) {
    checkState(
      isAtSea(),
      "Vessel %s cannot dock at %s while %s.",
      id,
      port,
      whereabouts
    );
    final Cell portCell = portGrid.getCell(port);
    checkState(
      getCell().equals(portCell),
      "Vessel %s cannot dock at %s from %s, which is not the port's cell %s.",
      id,
      port,
      getCell(),
      portCell
    );
    this.whereabouts = new Docked(port);
    currentEventManager().broadcast(new DockingEvent(this, port, dateTime));
  }

  /**
   * Undocks this vessel from the port it is docked at and broadcasts an
   * {@link UndockingEvent}.
   *
   * @param dateTime when the vessel undocks
   * @throws IllegalStateException if the vessel is not docked
   */
  public void undock(final LocalDateTime dateTime) {
    final Port port = getPortDockedAt();
    this.whereabouts = new AtSea();
    currentEventManager().broadcast(new UndockingEvent(this, port, dateTime));
  }

  /**
   * @return the event manager of the current trip, which forwards to this
   *         vessel's own, or this vessel's own if it is on no trip: event
   *         managers forward only upwards, so an event broadcast on the
   *         vessel's would never reach the trip's listeners
   */
  private EventManager currentEventManager() {
    return currentTrip == null ? eventManager : currentTrip.getEventManager();
  }

  @Override
  public String toString() {
    return id;
  }

  /** @return this vessel's current attributes */
  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  /**
   * Gives this vessel its starting state, its home port and its attributes,
   * and reconciles its whereabouts with them ({@link #reconcileWhereabouts}).
   * That is the vessel's initial state, not a docking, so it has no time and
   * broadcasts nothing; the statements a vessel applies later announce their
   * changes in whereabouts instead.
   *
   * @param homePortCode the code of the home port, or nothing if there is none
   * @param attributes   the attributes
   * @throws NullPointerException  if {@code attributes} holds a {@code null}
   *                               name or value
   * @throws IllegalStateException if the vessel is at sea and its home port or
   *                               behaviour does not resolve
   */
  void setStartingState(
    final Optional<String> homePortCode,
    final Map<String, ?> attributes
  ) {
    replaceAttributes(attributes);
    replaceHomePort(homePortCode);
    reconcileWhereabouts();
  }

  /** Puts {@code attributes} in place of this vessel's attributes. */
  private void replaceAttributes(final Map<String, ?> attributes) {
    this.attributes = ImmutableMap.copyOf(attributes);
  }

  /**
   * Looks up the port in the port grid with code {@code homePortCode} and makes
   * it this vessel's home port, or leaves the vessel without one if there is no
   * code or no such port. The vessel stays where it is.
   */
  private void replaceHomePort(final Optional<String> homePortCode) {
    this.homePort = homePortCode.flatMap(portGrid::getObject);
    this.homePortCell = homePort.map(portGrid::getCell);
  }

  /**
   * Undocks the vessel and starts the trip {@code newTrip} gives for the port
   * the vessel is docked at, which is the trip's origin. The trip may be of
   * any kind: its {@link TripBlock} chooses. Only a trip block opens a trip.
   *
   * @param newTrip gives the trip to start, for this vessel, from the given
   *                origin
   * @throws IllegalStateException    if the vessel is already on a trip, or is
   *                                  not docked
   * @throws IllegalArgumentException if the trip given is not this vessel's
   */
  void startTrip(final Function<? super Port, ? extends Trip> newTrip) {
    checkState(currentTrip == null, "Vessel %s is already on a trip.", id);
    checkState(isDocked(), "Vessel %s cannot start a trip at sea.", id);
    final Trip trip = newTrip.apply(getPortDockedAt());
    checkArgument(
      trip.getVessel() == this,
      "Vessel %s cannot start the trip of vessel %s.",
      id,
      trip.getVessel().getId()
    );
    undock(trip.getStartDateTime());
    this.currentTrip = trip;
    trip.start();
  }

  /**
   * Ends the current trip, charging {@code tripCosts} to it (see
   * {@link Trip#endTrip}), and folds its account into the vessel's own. Only
   * a {@link TripBlock} closes a trip.
   *
   * @param endDateTime when the trip ends
   * @param tripCosts   the costs of the finished trip, possibly none
   */
  void endTrip(
    final LocalDateTime endDateTime,
    final List<? extends Function<? super Trip, ? extends Money>> tripCosts
  ) {
    currentTrip.endTrip(endDateTime, tripCosts);
    this.account.add(currentTrip.getAccount());
    this.currentTrip = null;
  }
}
