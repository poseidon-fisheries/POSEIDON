/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

package uk.ac.ox.poseidon.fishing.tasks.travel;

import com.badlogic.gdx.ai.btree.Task;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.fishing.tasks.TaskFactory;
import uk.ac.ox.poseidon.fishing.vessels.TripBlock;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.geography.grids.Cell;

/**
 * A {@link TaskFactory} for a {@link TripBlock} running a whole fishing trip:
 * travel out, fish, turn back, travel back (reusing the same resolved
 * {@code travelTask} instance for both legs — safe because every task here
 * fully re-initializes its own state in {@code start()}, which gdx-ai calls
 * fresh each time a sequence enters a child, regardless of that child's
 * previous run), arrive in port, land the catch. Built via
 * {@link Factories#roundTrip}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class RoundTripFactory extends TaskFactory<TripBlock> {

  private final ValueFactory<
    ? super VesselComponentScope,
    ? super Vessel,
    Optional<Cell>
  > initialDestination;
  private final Factory<
    ? super VesselComponentScope,
    ? extends Task<Vessel>
  > travelTask;
  private final Factory<
    ? super VesselComponentScope,
    ? extends Task<Vessel>
  > fishingTask;
  private final Factory<
    ? super VesselComponentScope,
    ? extends Task<Vessel>
  > landingTask;
  private final ValueFactory<
    ? super VesselComponentScope,
    ? super Vessel,
    LocalDateTime
  > currentDateTime;

  /**
   * @return a {@link TripBlock} to {@code initialDestination} of:
   *         {@code travelTask} (outbound), {@code fishingTask}, a
   *         {@link SetDestinationToOrigin}, {@code travelTask} again
   *         (inbound), an {@link ArriveInPort} and {@code landingTask}
   */
  @Override
  protected TripBlock newInstance(final VesselComponentScope scope) {
    final Task<Vessel> travelTask = this.travelTask.get(scope);
    final Task<Vessel> fishingTask = this.fishingTask.get(scope);
    final Task<Vessel> landingTask = this.landingTask.get(scope);
    final Function<? super Vessel, LocalDateTime> currentDateTime =
      this.currentDateTime.get(scope);
    return new TripBlock(
      initialDestination.get(scope),
      currentDateTime,
      List.of(
        travelTask,
        fishingTask,
        new SetDestinationToOrigin(),
        travelTask,
        new ArriveInPort(currentDateTime),
        landingTask
      )
    );
  }
}
