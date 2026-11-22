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

package uk.ac.ox.poseidon.fishing.regulations.actions;

import com.google.common.collect.ImmutableMap;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.gears.Gear;
import uk.ac.ox.poseidon.fishing.vessels.values.HomePort;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.regulations.ExtendedAction;

/**
 * A {@link FishingAction} spanning a fixed {@link Duration}, starting at a
 * given time and place, fishing with a given {@link Gear}.
 */
@Getter
public class ExtendedFishingAction
  extends ExtendedAction<Vessel>
  implements TemporalFishingAction, SpatialFishingAction
{

  /** The gear the vessel fishes with over the action's duration. */
  @NonNull
  private final Gear gear;

  /** The vessel's attributes when it took the action. */
  private final Map<String, Object> attributes;

  /** The vessel's home port when it took the action. */
  @NonNull
  private final Port homePort;

  /**
   * @param vessel        the vessel taking the action
   * @param startDateTime when the action starts
   * @param duration      how long the action lasts
   * @param coordinate    where the action takes place
   * @param gear          the gear the vessel fishes with
   */
  public ExtendedFishingAction(
    @NonNull final Vessel vessel,
    @NonNull final LocalDateTime startDateTime,
    @NonNull final Duration duration,
    @NonNull final Coordinate coordinate,
    @NonNull final Gear gear
  ) {
    super(vessel, startDateTime, duration, coordinate);
    this.attributes = ImmutableMap.copyOf(vessel.getAttributes());
    this.homePort = new HomePort().apply(vessel);
    this.gear = gear;
  }

  /**
   * The action a vessel expects to take: before it fishes, a vessel can only
   * know how long fishing with {@code gear} is expected to take, not how long
   * it will take, so the action lasts the gear's expected duration.
   *
   * @param vessel        the vessel that would take the action
   * @param startDateTime when the action would start
   * @param cell          where the action would take place
   * @param gear          the gear the vessel would fish with
   * @return an action lasting {@code gear}'s
   *         {@linkplain Gear#getExpectedDuration() expected duration}
   */
  public static ExtendedFishingAction expected(
    final Vessel vessel,
    final LocalDateTime startDateTime,
    final Cell cell,
    final Gear gear
  ) {
    return new ExtendedFishingAction(
      vessel,
      startDateTime,
      gear.getExpectedDuration(),
      vessel.getVesselField().getModelGrid().toCoordinate(cell),
      gear
    );
  }
}
