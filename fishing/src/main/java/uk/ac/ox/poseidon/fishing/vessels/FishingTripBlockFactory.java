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

import com.badlogic.gdx.ai.btree.Task;
import com.google.common.collect.ImmutableList;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.joda.money.Money;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.fishing.tasks.TaskFactory;
import uk.ac.ox.poseidon.geography.grids.Cell;

/**
 * A {@link TaskFactory} for a {@link FishingTripBlock}. Built via
 * {@link Factories#fishingTrip}.
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class FishingTripBlockFactory extends TripBlockFactory {

  /**
   * @param initialDestination gives the trip's first destination for the
   *                           vessel, or none if it should not leave
   * @param currentDateTime    the clock, which dates the trip's start and end
   * @param tripCosts          give the costs of the finished trip, charged in
   *                           order; possibly none
   * @param children           the tasks run in sequence during the trip
   */
  public FishingTripBlockFactory(
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      Optional<Cell>
    > initialDestination,
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      LocalDateTime
    > currentDateTime,
    final ImmutableList<
      ? extends ValueFactory<
        ? super VesselComponentScope,
        ? super Trip,
        ? extends Money
      >
    > tripCosts,
    final ImmutableList<
      ? extends Factory<? super VesselComponentScope, ? extends Task<Vessel>>
    > children
  ) {
    super(initialDestination, currentDateTime, tripCosts, children);
  }

  @Override
  FishingTripBlock newTripBlock(
    final Function<? super Vessel, Optional<Cell>> initialDestination,
    final Function<? super Vessel, LocalDateTime> currentDateTime,
    final List<? extends Function<? super Trip, ? extends Money>> tripCosts,
    final List<? extends Task<Vessel>> children
  ) {
    return new FishingTripBlock(
      initialDestination,
      currentDateTime,
      tripCosts,
      children
    );
  }
}
