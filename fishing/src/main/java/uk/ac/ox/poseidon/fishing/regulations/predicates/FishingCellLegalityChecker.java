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

package uk.ac.ox.poseidon.fishing.regulations.predicates;

import static lombok.AccessLevel.PACKAGE;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import sim.util.Int2D;
import uk.ac.ox.poseidon.fishing.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.engines.Engine;
import uk.ac.ox.poseidon.fishing.vessels.gears.Gear;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.paths.PathLengthCalculator;
import uk.ac.ox.poseidon.regulations.Regulations;

/**
 * Predicate matching cells the vessel it is given could legally fish at:
 * builds a hypothetical {@link ExtendedFishingAction} for arriving at the
 * candidate cell (travel time included, via {@code pathLengthCalculator}) and
 * lasting {@code fishingDuration}, and checks it against {@code regulations}.
 * Used to filter fishing-cell candidates before committing to a trip
 * there.
 * <p>
 * {@code fishingDuration} is the typical duration of a fishing action: fishers
 * don't know how long a given set or haul will take, so they plan with a
 * typical one. A fishing action expected to run into a closure is refused here;
 * one that actually runs longer than planned is not this checker's concern.
 */
@RequiredArgsConstructor(access = PACKAGE)
public class FishingCellLegalityChecker
  implements BiFunction<Vessel, Int2D, Boolean>
{

  private final VesselComponent<? extends Gear> gear;
  private final VesselComponent<? extends Engine> engine;
  private final Regulations<? super ExtendedFishingAction> regulations;
  private final PathLengthCalculator pathLengthCalculator;
  private final Duration fishingDuration;
  private final Function<? super Vessel, LocalDateTime> currentDateTime;

  /**
   * @param vessel          the vessel that would fish
   * @param fishingCell the cell it would fish at
   * @return whether fishing at {@code fishingCell} would currently be
   *         permitted for {@code vessel}
   */
  @Override
  public Boolean apply(final Vessel vessel, final Int2D fishingCell) {
    return regulations.isPermitted(makeAction(vessel, fishingCell));
  }

  /**
   * Builds the {@link ExtendedFishingAction} the vessel would plan at
   * {@code fishingCell}: starting whenever it would arrive there from its
   * current cell, and lasting {@code fishingDuration}.
   *
   * @throws IllegalArgumentException if no path exists from the vessel's
   *                                  current cell to {@code fishingCell}
   */
  private ExtendedFishingAction makeAction(
    final Vessel vessel,
    final Int2D fishingCell
  ) {
    final Duration travelDuration = DistanceCalculator.travelDuration(
      pathLengthCalculator.lengthInKm(vessel.getCell(), fishingCell),
      engine.of(vessel).getCruisingSpeedInKph()
    );

    return new ExtendedFishingAction(
      vessel,
      currentDateTime.apply(vessel).plus(travelDuration),
      fishingDuration,
      vessel.getVesselField().getModelGrid().toCoordinate(fishingCell),
      gear.of(vessel)
    );
  }
}
