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
import uk.ac.ox.poseidon.geography.grids.Cell;

/**
 * A composite task that opens a trip, runs its children in sequence and
 * closes the trip once they have all succeeded. Only a trip block sets or
 * clears the vessel's current trip, so a behaviour that is not running has
 * no trip open.
 *
 * <p>When it starts running, it asks {@code initialDestination} for the
 * trip's first destination and fails, without opening a trip, if there is
 * none. Once the trip is open, the block may not end any other way than by
 * closing it: a child that fails, or a cancellation of the block, throws, as
 * a trip left open at sea is a model error. Aborting a trip is modelled
 * inside it, by a task that heads home.
 */
public final class TripBlock extends Sequence<Vessel> {

  private final Function<? super Vessel, Optional<Cell>> initialDestination;
  private final Function<? super Vessel, LocalDateTime> currentDateTime;
  private boolean tripOpen;

  /**
   * @param initialDestination gives the trip's first destination for the
   *                           vessel, or none if it should not leave
   * @param currentDateTime    the clock, which dates the trip's start and end
   * @param children           the tasks run in sequence during the trip
   * @throws IllegalArgumentException if {@code children} is empty
   */
  public TripBlock(
    final Function<? super Vessel, Optional<Cell>> initialDestination,
    final Function<? super Vessel, LocalDateTime> currentDateTime,
    final List<? extends Task<Vessel>> children
  ) {
    checkArgument(!children.isEmpty(), "A trip needs at least one task.");
    this.initialDestination = initialDestination;
    this.currentDateTime = currentDateTime;
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
      getVessel().startTrip(
        destination.get(),
        currentDateTime.apply(getVessel())
      );
      tripOpen = true;
    }
    super.run();
  }

  /**
   * Closes the trip if the block succeeded.
   *
   * @throws IllegalStateException if the block failed or was cancelled with
   *                               its trip open
   */
  @Override
  public void end() {
    if (tripOpen) {
      checkState(
        getStatus() == SUCCEEDED,
        "A trip block ended as %s with its trip open: a trip ends only by " +
          "travelling home.",
        getStatus()
      );
      getVessel().endTrip(currentDateTime.apply(getVessel()));
      tripOpen = false;
    }
  }

  private Vessel getVessel() {
    final Vessel vessel = getObject();
    checkState(vessel != null, "%s read its vessel outside a step.", this);
    return vessel;
  }
}
