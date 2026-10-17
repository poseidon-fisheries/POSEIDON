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

package uk.ac.ox.poseidon.agents.vessels;

import static com.badlogic.gdx.ai.btree.Task.Status.RUNNING;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static lombok.AccessLevel.NONE;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import lombok.Getter;
import lombok.NonNull;
import sim.engine.Steppable;
import sim.portrayal.Oriented2D;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.vessels.accounts.Account;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * A fishing vessel, the model's agent: it has components (its behaviour, gear,
 * engine, hold and the rest, each read through its {@link VesselComponent}
 * declaration) and a home port. Its behaviour is the root task of a gdx-ai
 * behaviour tree, which the vessel runs: it repeatedly steps the tree,
 * rescheduling itself either after the running task's {@link #taskDuration}
 * (while the behaviour is running) or on the very next tick (once it isn't).
 * It steps its behaviour only while {@link #isActive()} holds.
 * <p>
 * Its modifiable characteristics (name, account, home port, components,
 * registration and tags) are changed from outside its behaviour, by the fleet
 * register, so their setters go through {@link #mutate}, which queues them
 * while the behaviour is running: they take effect between runs of the
 * behaviour, never during one, so a vessel isn't deactivated at sea or given a
 * new hold with its catch on board. Its current state (cell, heading, trips)
 * is changed by the behaviour itself and changes immediately.
 * <p>
 * A vessel has at most one step pending on the schedule. The vessel isn't
 * itself a {@link Steppable}: what goes on the schedule is its private
 * {@link #stepper}, so nothing outside the vessel can schedule it, and every
 * step is scheduled through {@link #scheduleNextStep}, which refuses to
 * schedule a second one. Outside a step, an active vessel always has a step
 * pending ({@link #mutate} restores this after each mutation). Becoming
 * inactive doesn't cancel a pending step; the step still runs, finds the
 * vessel inactive and schedules nothing, so a vessel can be inactive with a
 * step pending, and reactivating it then needs no new step.
 */
@Getter
public class Vessel implements Oriented2D {

  private static final int VESSEL_STEP_ORDERING = 1;

  private final @NonNull TemporalSchedule schedule;
  private final @NonNull EventManager eventManager;

  /** The products of the factories built for this vessel. */
  private final ProductStore productStore = new ProductStore();

  /**
   * The declaration of the behaviour this vessel runs, the root task of its
   * tree; a vessel without that component does not act.
   */
  @Getter(NONE)
  private final @NonNull VesselComponent<? extends Task<Vessel>> behaviour;

  /**
   * The tree this vessel runs over its behaviour component, renewed whenever
   * that component is built, between runs of the tree; {@code null} until
   * then. It has this vessel as its object only during a step, so a task
   * reading its vessel between steps fails at once.
   */
  @Getter(NONE)
  private BehaviorTree<Vessel> tree;

  @Getter(NONE)
  private final Queue<Runnable> mutationQueue = new LinkedList<>();

  /**
   * How long until the running behaviour's task is executed again: set by that
   * task when it reports {@code RUNNING} (see
   * {@link uk.ac.ox.poseidon.agents.tasks.VesselTask#runFor}), and cleared once
   * this vessel's next step is scheduled, so each running step must set its
   * own.
   */
  @Getter(NONE)
  private Duration taskDuration;

  /**
   * What this vessel puts on the schedule: runs {@link #step()}. Private, so
   * only it can.
   */
  @Getter(NONE)
  private final Steppable stepper = _ -> step();

  /** Whether this vessel has a step on the schedule that hasn't run yet. */
  @Getter(NONE)
  private boolean stepPending;

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
   * once it becomes active, through {@link #mutate}: a vessel never starts
   * active, so an active vessel always has a step pending.
   *
   * @param schedule     the schedule this vessel reschedules itself on after
   *                     each step
   * @param eventManager the event manager this vessel broadcasts on
   * @param id           the vessel's identifying id
   * @param vesselField  the field the vessel moves within
   * @param portGrid     the grid of ports the vessel can dock at
   * @param marketGrid   the grid of markets the vessel can sell into
   * @param behaviour    the declaration of the behaviour the vessel steps
   */
  @SuppressFBWarnings(
    value = "EI2",
    justification = "Vessel keeps references to mutable schedule/event manager by design."
  )
  public Vessel(
    @NonNull final TemporalSchedule schedule,
    @NonNull final EventManager eventManager,
    @NonNull final String id,
    @NonNull final VesselField vesselField,
    @NonNull final PortGrid portGrid,
    @NonNull final MarketGrid marketGrid,
    @NonNull final VesselComponent<? extends Task<Vessel>> behaviour
  ) {
    this.schedule = schedule;
    this.eventManager = eventManager;
    this.id = id;
    this.vesselField = vesselField;
    this.portGrid = portGrid;
    this.marketGrid = marketGrid;
    this.behaviour = behaviour;
  }

  /** @return this vessel's tags, unmodifiable */
  @SuppressWarnings("unused")
  public @NonNull Map<String, Object> getTags() {
    return Collections.unmodifiableMap(tags);
  }

  private final @NonNull Map<String, Object> tags = new HashMap<>();

  // Modifiable characteristics
  /** This vessel's display name. */
  private String name;
  /** This vessel's running balance. */
  private Account account;
  /** This vessel's home port. */
  private Port homePort;

  /**
   * The store this vessel keeps for each of its components, by the
   * declaration the component is built from, in the order they were first
   * built.
   */
  @Getter(NONE)
  private final Map<VesselComponent<?>, ProductStore> componentStores =
    new LinkedHashMap<>();

  /** This vessel's components, by the declaration each is built from. */
  @Getter(NONE)
  private final Map<VesselComponent<?>, Object> components =
    new LinkedHashMap<>();

  /**
   * Whether this vessel is currently registered as active (see
   * {@link #setRegisteredAsActive}).
   */
  private boolean registeredAsActive;

  // Current state variables
  /** This vessel's current heading, in radians. */
  private double heading;
  /** The vessel's current trip, or {@code null} if it isn't on one. */
  private Trip currentTrip;
  /**
   * The vessel's last finished trip, or {@code null} if it hasn't finished one
   * yet.
   */
  private Trip previousTrip;

  /**
   * @return whether this vessel has its behaviour, is
   *         {@link #registeredAsActive}, every gear among its components is
   *         active, and it has a home port
   */
  public boolean isActive() {
    return (
      tree != null &&
      registeredAsActive &&
      components
        .values()
        .stream()
        .noneMatch(
          component -> component instanceof final Gear gear && !gear.isActive()
        ) &&
      homePort != null
    );
  }

  /** @return whether this vessel's behaviour tree is running */
  boolean isBehaviourRunning() {
    return tree != null && tree.getStatus() == RUNNING;
  }

  /**
   * Drains any queued mutations (only once the behaviour isn't running), then,
   * if still active, steps the behaviour tree with this vessel and reschedules —
   * after {@link #taskDuration} if it's now running, or on the next tick
   * otherwise.
   */
  private void step() {
    stepPending = false;
    if (!isBehaviourRunning()) {
      while (!mutationQueue.isEmpty()) {
        mutationQueue.poll().run();
      }
    }
    if (isActive()) {
      tree.setObject(this);
      try {
        tree.step();
      } finally {
        tree.setObject(null);
      }
      if (isBehaviourRunning()) {
        final Duration delay = checkNotNull(
          taskDuration,
          "Vessel %s's behaviour is running, but no task set a duration this step.",
          this
        );
        taskDuration = null;
        scheduleNextStep(delay);
      } else {
        checkState(
          taskDuration == null,
          "Vessel %s's behaviour is not running, but a task set a duration this step.",
          this
        );
        scheduleNextStep();
      }
    }
  }

  /** Schedules this vessel's next step on the next tick. */
  private void scheduleNextStep() {
    checkState(!stepPending, "Vessel %s already has a step pending.", this);
    stepPending = schedule.scheduleOnce(stepper, VESSEL_STEP_ORDERING);
  }

  /** Schedules this vessel's next step after {@code delay}. */
  private void scheduleNextStep(final Duration delay) {
    checkState(!stepPending, "Vessel %s already has a step pending.", this);
    stepPending = schedule.scheduleOnceIn(delay, stepper, VESSEL_STEP_ORDERING);
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
   * Applies a change from outside the vessel's behaviour, between runs of the
   * behaviour. If the behaviour is running, we queue the mutation, and it will
   * be applied the next time the vessel is stepped and the behaviour is not
   * running.
   * <p>
   * Otherwise, we apply the mutation immediately and then restore the invariant
   * that, outside a step, an active vessel has a step pending: if the vessel
   * is active and has none, we schedule one. This depends only on the
   * vessel's state after the mutation, not on what it was before, so a
   * sequence of mutations that briefly activates and deactivates the vessel
   * (as a fleet event's setters can) never leaves it with more than one step
   * pending.
   */
  void mutate(final Runnable mutation) {
    if (isBehaviourRunning()) {
      mutationQueue.add(mutation);
    } else {
      mutation.run();
      if (isActive() && !stepPending) {
        scheduleNextStep();
      }
    }
  }

  /** Sets the vessel's display name. */
  public void setName(final String name) {
    mutate(() -> this.name = name);
  }

  /** Sets the vessel's running balance. */
  public void setAccount(final Account account) {
    mutate(() -> this.account = account);
  }

  /**
   * Sets the vessel's home port, moving it there if it has no current cell yet.
   */
  public void setHomePort(final Port homePort) {
    mutate(() -> {
      this.homePort = homePort;
      // TODO: if location were to be stored in port we wouldn't need the port
      // grid here
      if (this.homePort != null && getCell() == null) setCurrentCell(
        portGrid.getLocation(homePort)
      );
    });
  }

  /**
   * Builds the component {@code declaration} declares under a scope carrying
   * the store this vessel keeps for it, opened the first time, and makes it
   * this vessel's component for that declaration. Built at once, it replaces
   * the previous one through {@link #mutate}, between runs of the behaviour.
   *
   * @param declaration the declaration of the component
   * @param scope       this vessel's scope
   * @return the component built
   */
  <T> T buildComponent(
    final VesselComponent<T> declaration,
    final VesselScope scope
  ) {
    final ProductStore store = componentStores.computeIfAbsent(declaration, _ ->
      new ProductStore()
    );
    final T component = declaration
      .getFactory()
      .get(new VesselComponentScope(scope, store));
    mutate(() -> {
      components.put(declaration, component);
      if (declaration == behaviour) {
        tree = new BehaviorTree<>(behaviour.of(this));
      }
    });
    return component;
  }

  /**
   * @param declaration the declaration of the component
   * @return this vessel's component for {@code declaration}, if it has one
   * @throws IllegalStateException if this vessel was not created with
   *                               {@code declaration}: a wiring error, not an
   *                               absence
   */
  // The vessel keeps under a declaration only what its factory built, a T
  @SuppressWarnings("unchecked")
  <T> Optional<T> findComponent(final VesselComponent<T> declaration) {
    checkState(
      componentStores.containsKey(declaration),
      "Vessel %s was not created with the declaration %s",
      id,
      declaration
    );
    return Optional.ofNullable((T) components.get(declaration));
  }

  /** Sets whether the vessel is registered as active. */
  public void setRegisteredAsActive(final boolean registeredAsActive) {
    mutate(() -> this.registeredAsActive = registeredAsActive);
  }

  /** @return {@link #heading} */
  @Override
  public double orientation2D() {
    return heading;
  }

  /** Points the vessel's heading towards {@code destinationCell}'s centre. */
  public void setHeadingTowards(final Int2D destinationCell) {
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
  public Int2D getCell() {
    return vesselField.getCell(this);
  }

  /** Moves the vessel to {@code cell}. */
  public void setCurrentCell(final Int2D cell) {
    vesselField.setCell(this, cell);
  }

  /** @return {@code true} if the vessel's current cell has a port on it */
  public boolean isAtPort() {
    return portGrid.anyObjectsAt(getCell());
  }

  @Override
  public String toString() {
    return name == null ? id : name + " (" + id + ")";
  }

  /**
   * @return {@code true} if the vessel's current cell is its home port's cell
   */
  public boolean isAtHomePort() {
    return getCell().equals(getHomePortLocation());
  }

  /** @return the home port's grid cell */
  public Int2D getHomePortLocation() {
    return portGrid.getLocation(homePort);
  }

  /** Sets (or replaces) a tag by name. */
  public void putTag(final String tagName, final Object tagValue) {
    mutate(() -> tags.put(tagName, tagValue));
  }

  /** @return the named tag's value, if set */
  public Optional<Object> getTag(final String tagName) {
    return Optional.ofNullable(tags.get(tagName));
  }

  /**
   * Starts a new {@link Trip} to {@code destination}. Only a
   * {@link TripBlock} opens a trip.
   *
   * @throws IllegalStateException if the vessel is already on a trip
   */
  void startTrip(final Int2D destination) {
    checkState(currentTrip == null, "Vessel %s is already on a trip.", id);
    this.currentTrip = new Trip(this, destination);
  }

  /**
   * Records the current trip's arrival in port; see
   * {@link Trip#arriveInPort()}.
   */
  public void arriveInPort() {
    currentTrip.arriveInPort();
  }

  /**
   * Ends the current trip, folding its account into the vessel's own, and keeps
   * it as the {@link #previousTrip}. Only a {@link TripBlock} closes a trip.
   */
  void endTrip() {
    currentTrip.endTrip();
    this.account.add(currentTrip.getAccount());
    this.previousTrip = currentTrip;
    this.currentTrip = null;
  }
}
