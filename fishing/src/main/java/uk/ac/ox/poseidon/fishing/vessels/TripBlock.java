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

import static com.badlogic.gdx.ai.btree.Task.Status.SUCCEEDED;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.joda.money.Money;
import uk.ac.ox.poseidon.geography.Destination;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.ports.Port;

/**
 * A composite task that opens a trip, runs its children in sequence and
 * closes the trip once they have all succeeded. It opens a plain
 * {@link Trip}; a {@link FishingTripBlock} opens a {@link FishingTrip}. Only a
 * trip block sets or clears the vessel's current trip, so a behaviour that is
 * not running has no trip open.
 *
 * <p>It has trip costs, possibly none, each a function of the finished trip,
 * which it charges as it closes the trip, before the trip's end is broadcast.
 *
 * <p>When it starts running, it asks {@code initialDestination} for the
 * trip's first destination and fails, without opening a trip, if there is
 * none. Once the trip is open, the block may not end any other way than by
 * closing it: a child that fails, or a cancellation of the block, throws, as
 * a trip left open at sea is a model error. Aborting a trip is modelled
 * inside it, by a task that heads home.
 */
public class TripBlock extends Sequence<Vessel> {

  private final Function<? super Vessel, Optional<Cell>> initialDestination;
  private final Function<? super Vessel, LocalDateTime> currentDateTime;
  private final List<
    ? extends Function<? super Trip, ? extends Money>
  > tripCosts;
  private boolean tripOpen;

  /**
   * @param initialDestination gives the trip's first destination for the
   *                           vessel, or none if it should not leave
   * @param currentDateTime    the clock, which dates the trip's start and end
   * @param tripCosts          the costs of the finished trip, charged in
   *                           order; possibly none
   * @param children           the tasks run in sequence during the trip
   * @throws IllegalArgumentException if {@code children} is empty
   */
  TripBlock(
    final Function<? super Vessel, Optional<Cell>> initialDestination,
    final Function<? super Vessel, LocalDateTime> currentDateTime,
    final List<? extends Function<? super Trip, ? extends Money>> tripCosts,
    final List<? extends Task<Vessel>> children
  ) {
    checkArgument(!children.isEmpty(), "A trip needs at least one task.");
    this.initialDestination = initialDestination;
    this.currentDateTime = currentDateTime;
    this.tripCosts = List.copyOf(tripCosts);
    children.forEach(this::addChild);
  }

  @Override
  public void start() {
    super.start();
    tripOpen = false;
  }

  /**
   * Opens the trip on the block's first run, or fails if there is no
   * destination, then runs the current child.
   */
  @Override
  public void run() {
    if (!tripOpen) {
      final Optional<Cell> destination = initialDestination.apply(getVessel());
      if (destination.isEmpty()) {
        fail();
        return;
      }
      final Vessel vessel = getVessel();
      final LocalDateTime startDateTime = currentDateTime.apply(vessel);
      vessel.startTrip(origin ->
        newTrip(vessel, origin, destination.get(), startDateTime)
      );
      tripOpen = true;
    }
    super.run();
  }

  /**
   * Closes the trip, charging its cost, if the block succeeded.
   *
   * @throws IllegalStateException if the block failed or was cancelled with
   *                               its trip open
   */
  @Override
  public void end() {
    if (tripOpen) {
      checkState(
        getStatus() == SUCCEEDED,
        "A trip block ended as %s with its trip open: a trip ends only " +
          "docked in port.",
        getStatus()
      );
      getVessel().endTrip(currentDateTime.apply(getVessel()), tripCosts);
      tripOpen = false;
    }
  }

  /**
   * @return a new trip of this block's kind, a plain {@link Trip}, for
   *         {@code vessel} to start
   */
  Trip newTrip(
    final Vessel vessel,
    final Port origin,
    final Destination destination,
    final LocalDateTime startDateTime
  ) {
    return new Trip(vessel, origin, destination, startDateTime);
  }

  private Vessel getVessel() {
    final Vessel vessel = getObject();
    checkState(vessel != null, "%s read its vessel outside a step.", this);
    return vessel;
  }
}
