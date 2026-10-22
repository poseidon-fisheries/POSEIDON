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
import java.util.function.Function;
import lombok.Getter;
import lombok.NonNull;
import sim.portrayal.Oriented2D;
import sim.util.Double2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.market.MarketGrid;
import uk.ac.ox.poseidon.fishing.vessels.Fleet.StepScheduler;
import uk.ac.ox.poseidon.fishing.vessels.VesselStatement.AttributeChange;
import uk.ac.ox.poseidon.fishing.vessels.VesselStatement.Exit;
import uk.ac.ox.poseidon.fishing.vessels.accounts.Account;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.Destination;
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
 * tick (once it isn't). It steps its behaviour only while {@link #isActive()}
 * holds.
 * <p>
 * What a source states about it, a change of attributes or its exit, joins
 * its queue of statements and takes effect at the start of its next step in
 * which the behaviour is not running, never during a run of the behaviour and
 * never inside the call that made the statement: so a vessel doesn't exit at
 * sea, and a change takes effect in port. A statement queued on a vessel with
 * no step pending schedules one, at once. Once the queue is applied, every
 * component whose choice reads an attribute that changed is rebuilt, once;
 * the others, and what the vessel's own store holds, stay. An exit ends the
 * queue: the vessel leaves its fleet with no component left, and so inactive,
 * and nothing after it in the queue applies, nor any rebuild. Its current
 * state (cell, heading, the port it is docked at, trips) is changed by the
 * behaviour itself, at once.
 * <p>
 * A vessel is either docked at a port or at sea. It starts docked at its home
 * port, where it is created. Docking ({@link #dockAt}) and undocking
 * ({@link #undock}) are explicit acts, each at a time, and the vessel cannot
 * move while docked: a cell can hold several ports, so the port a vessel is
 * docked at is never inferred from its cell.
 * <p>
 * A vessel has at most one step pending on the schedule. It holds no
 * schedule: its fleet gives it a {@link StepScheduler} that schedules its step
 * and nothing else, and refuses to schedule a second one. Nothing outside the
 * vessel can schedule its step. Its first step is at the instant it is
 * created; outside a step, an active vessel always has a step pending. A
 * vessel that becomes inactive schedules nothing more, until a statement
 * gives it a step.
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
   * component does not act), and the attributes giving its home port and name.
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

  // TODO: consider if the vessel really needs a reference to the market grid
  /** The grid of markets this vessel can sell into. */
  private final @NonNull MarketGrid marketGrid;

  /**
   * Creates an inactive vessel, with no behaviour built yet. It starts stepping
   * once its first step is scheduled ({@link #scheduleFirstStep}).
   *
   * @param newStepScheduler makes, from this vessel's step, what schedules
   *                         that step and nothing else
   * @param newFleetExit     makes, from this vessel, what takes it out of its
   *                         fleet and nothing else
   * @param eventManager     the event manager this vessel broadcasts on
   * @param id               the vessel's identifying id
   * @param vesselField      the field the vessel moves within
   * @param portGrid         the grid of ports the vessel can dock at
   * @param marketGrid       the grid of markets the vessel can sell into
   * @param specification    what the vessel is made of
   */
  Vessel(
    @NonNull final Function<Runnable, StepScheduler> newStepScheduler,
    @NonNull final Function<Vessel, Runnable> newFleetExit,
    @NonNull final EventManager eventManager,
    @NonNull final String id,
    @NonNull final VesselField vesselField,
    @NonNull final PortGrid portGrid,
    @NonNull final MarketGrid marketGrid,
    @NonNull final VesselSpecification specification
  ) {
    this.stepScheduler = newStepScheduler.apply(this::step);
    this.fleetExit = newFleetExit.apply(this);
    this.eventManager = eventManager;
    this.id = id;
    this.vesselField = vesselField;
    this.portGrid = portGrid;
    this.marketGrid = marketGrid;
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
   * The port in the port grid whose code the home port attribute gave when the
   * attributes were last set, or {@code null} if none; looked up then, not on
   * each read, because it is read on every valuation of a fishing option.
   */
  @Getter(NONE)
  private Port homePort;

  /**
   * The cell {@link #homePort} is placed on, or {@code null} if there is no
   * home port; looked up with it, since ports do not move and the cell is read
   * on every route to a fishing option.
   */
  @Getter(NONE)
  private Cell homePortCell;

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
  /** The port this vessel is docked at, or {@code null} if it is at sea. */
  private Port portDockedAt;
  /** The vessel's current trip, or {@code null} if it isn't on one. */
  private Trip currentTrip;
  /**
   * The vessel's last finished trip, or {@code null} if it hasn't finished one
   * yet.
   */
  private Trip previousTrip;

  /** @return whether this vessel has its behaviour and a home port */
  public boolean isActive() {
    return tree != null && getHomePort() != null;
  }

  /** @return whether this vessel's behaviour tree is running */
  private boolean isBehaviourRunning() {
    return tree != null && tree.getStatus() == RUNNING;
  }

  /**
   * Steps the running task, if the behaviour tree is running. If the tree is
   * then not running, applies the queued statements and, if the vessel has not
   * exited and is active, starts the tree afresh, in this same step. Then, if
   * the vessel is active, schedules the next step after {@link #taskDuration}.
   *
   * @throws IllegalStateException if the tree, started afresh, finishes
   *                               without starting a task: restarting it would
   *                               never move time on
   */
  private void step() {
    if (isBehaviourRunning()) {
      stepBehaviour();
    }
    if (!isBehaviourRunning()) {
      applyStatements();
      if (isActive()) {
        stepBehaviour();
        checkState(
          isBehaviourRunning(),
          "Vessel %s's behaviour finished without starting a task.",
          this
        );
      }
    }
    if (isActive()) {
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
   * Applies the queued statements in order and rebuilds what their changes
   * choose anew; an exit ends this vessel, and the statements after it are
   * never applied.
   */
  private void applyStatements() {
    final Map<String, Object> previousAttributes = attributes;
    while (!statements.isEmpty()) {
      switch (statements.poll()) {
        case final AttributeChange change -> setAttributes(
          change.applyTo(attributes)
        );
        case final Exit _ -> {
          exit();
          return;
        }
      }
    }
    rebuildChosenAnew(previousAttributes);
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
   * Ends this vessel's component stores, in the reverse of the order its
   * specification builds them, and drops the components, which are no longer
   * its own, so that it has no behaviour and is inactive; ends its own store;
   * takes it off the vessel field; and takes it out of its fleet, which
   * announces the exit.
   */
  private void exit() {
    specification
      .listedComponents()
      .toList()
      .reversed()
      .forEach(this::dropComponent);
    tree = null;
    productStore.end();
    vesselField.remove(this);
    fleetExit.run();
  }

  /**
   * Builds every component this vessel's specification lists, in its order:
   * the behaviour first, then the others as listed.
   */
  void buildComponents() {
    specification.listedComponents().forEach(this::buildComponent);
  }

  /**
   * Builds what {@code component} gives this vessel, if anything: under a
   * scope carrying a fresh store, which this vessel keeps for it. Renews the
   * tree if that is the behaviour. The behaviour must not be running, and
   * this vessel must have nothing built for {@code component}.
   */
  private <T> void buildComponent(final VesselComponent<T> component) {
    factoryFor(component).ifPresent(factory -> {
      final ProductStore store = new ProductStore();
      componentStores.put(component, store);
      components.put(
        component,
        factory.get(
          new VesselComponentScope(specification.scopeOf(this), store)
        )
      );
    });
    if (component == specification.getBehaviour()) {
      tree = specification
        .getBehaviour()
        .findIn(this)
        .map(task -> new BehaviorTree<Vessel>(task))
        .orElse(null);
    }
  }

  /**
   * @return what builds {@code component} for this vessel: its single
   *         factory, or what its choice gives for this vessel's attributes;
   *         nothing if that is no component
   */
  private <T> Optional<
    Factory<? super VesselComponentScope, ? extends T>
  > factoryFor(final VesselComponent<T> component) {
    return switch (component) {
      case final FactoryComponent<T> single -> Optional.of(single.getFactory());
      case final ChosenComponent<T> chosen -> switch (chosen.outcomeFor(this)) {
        case final BuildOutcome<? extends T> build -> Optional.of(
          build.getFactory()
        );
        case final NoComponent<? extends T> _ -> Optional.empty();
      };
    };
  }

  /**
   * Gives this vessel anew every component whose choice reads an attribute
   * whose value differs from {@code previousAttributes}, even if the same row
   * is picked: ends their stores in the reverse of the order its
   * specification builds them, then builds them in that order.
   */
  private void rebuildChosenAnew(final Map<String, Object> previousAttributes) {
    final List<VesselComponent<?>> componentsToRebuild = specification
      .listedComponents()
      .filter(
        component ->
          component instanceof final ChosenComponent<?> chosen &&
          chosen
            .getAttributes()
            .stream()
            .anyMatch(
              attribute ->
                !Objects.equals(
                  previousAttributes.get(attribute),
                  attributes.get(attribute)
                )
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

  /** @return the vessel's continuous-space position */
  public Double2D getPoint() {
    return vesselField.getPoint(this);
  }

  /** @return the vessel's position, converted to a geographic coordinate */
  public Coordinate getCoordinate() {
    return vesselField.getModelGrid().toCoordinate(getPoint());
  }

  /** @return the grid cell the vessel currently occupies */
  public Cell getCell() {
    return vesselField.getCell(this);
  }

  /**
   * Moves the vessel to {@code cell}.
   *
   * @throws IllegalStateException if the vessel is docked
   */
  public void setCurrentCell(final Cell cell) {
    checkState(
      !isDocked(),
      "Vessel %s cannot move while docked at %s.",
      id,
      portDockedAt
    );
    vesselField.setCell(this, cell);
  }

  /** @return whether this vessel is docked at a port */
  public boolean isDocked() {
    return portDockedAt != null;
  }

  /**
   * Docks this vessel at {@code port} and broadcasts a {@link DockingEvent}.
   *
   * @param port     the port to dock at
   * @param dateTime when the vessel docks
   * @throws IllegalStateException if the vessel is already docked, or is not in
   *                               {@code port}'s cell
   */
  public void dockAt(final Port port, final LocalDateTime dateTime) {
    checkState(
      !isDocked(),
      "Vessel %s is already docked at %s.",
      id,
      portDockedAt
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
    this.portDockedAt = port;
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
    checkState(isDocked(), "Vessel %s is not docked.", id);
    final Port port = portDockedAt;
    this.portDockedAt = null;
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
    return getName()
      .map(name -> name + " (" + id + ")")
      .orElse(id);
  }

  /**
   * @return this vessel's name, the value of the attribute its specification
   *         names for it, or nothing if it has none
   */
  public Optional<String> getName() {
    return specification
      .getNameAttribute()
      .map(attributes::get)
      .map(Object::toString);
  }

  /**
   * @return this vessel's home port, the port in the port grid whose code was
   *         the value of the attribute its specification names for it when
   *         its attributes were last set, or {@code null} if it had no such
   *         attribute or no port had that code
   */
  public Port getHomePort() {
    return homePort;
  }

  /**
   * @return {@code true} if the vessel's current cell is its home port's cell
   */
  public boolean isAtHomePort() {
    return getCell().equals(getHomePortCell());
  }

  /** @return the home port's grid cell */
  public Cell getHomePortCell() {
    return homePortCell;
  }

  /** @return this vessel's current attributes */
  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  /**
   * Puts {@code attributes} in place of this vessel's attributes, and places
   * the vessel docked at its home port if it has no position yet: that is its
   * initial state, not a docking, so it has no time and broadcasts nothing.
   *
   * @throws NullPointerException if {@code attributes} holds a {@code null}
   *                              name or value
   */
  void setAttributes(final Map<String, ?> attributes) {
    final ImmutableMap<String, Object> newAttributes = ImmutableMap.copyOf(
      attributes
    );
    this.attributes = newAttributes;
    this.homePort = Optional.ofNullable(
      newAttributes.get(specification.getHomePortAttribute())
    )
      .flatMap(code -> portGrid.getObject(code.toString()))
      .orElse(null);
    this.homePortCell = homePort == null ? null : portGrid.getCell(homePort);
    if (homePortCell != null && getCell() == null) {
      vesselField.setCell(this, homePortCell);
      this.portDockedAt = homePort;
    }
  }

  /**
   * Undocks the vessel and starts a new {@link Trip} to {@code destination},
   * from the port it was docked at. Only a {@link TripBlock} opens a trip.
   *
   * @param destination   where the trip is initially headed
   * @param startDateTime when the trip starts
   * @throws IllegalStateException if the vessel is already on a trip, or is
   *                               not docked
   */
  void startTrip(
    final Destination destination,
    final LocalDateTime startDateTime
  ) {
    checkState(currentTrip == null, "Vessel %s is already on a trip.", id);
    checkState(isDocked(), "Vessel %s cannot start a trip at sea.", id);
    final Port origin = portDockedAt;
    undock(startDateTime);
    this.currentTrip = new Trip(this, origin, destination, startDateTime);
  }

  /**
   * Ends the current trip, folding its account into the vessel's own, and keeps
   * it as the {@link #previousTrip}. Only a {@link TripBlock} closes a trip.
   *
   * @param endDateTime when the trip ends
   */
  void endTrip(final LocalDateTime endDateTime) {
    currentTrip.endTrip(endDateTime);
    this.account.add(currentTrip.getAccount());
    this.previousTrip = currentTrip;
    this.currentTrip = null;
  }
}
