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

import java.time.LocalDateTime;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.fishing.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.gears.Gear;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.regulations.Regulations;

/**
 * Predicate matching cells the vessel it is given could legally fish at:
 * builds the {@linkplain ExtendedFishingAction#expected expected fishing
 * action} at the candidate cell, starting when {@code expectedFishingStart}
 * says the vessel would start fishing there, and checks it against
 * {@code regulations}. Used to filter fishing-cell candidates before committing
 * to a trip there. How the start comes about (travel, waiting for fishing
 * hours) is the mapping's business, not this checker's.
 * <p>
 * The action lasts the vessel's gear's expected duration: fishers don't know
 * how long a given set or haul will take, so they plan with the expected one.
 * A fishing action expected to run into a closure is refused here; one that
 * actually runs longer than expected is not this checker's concern.
 */
@RequiredArgsConstructor(access = PACKAGE)
public class FishingCellLegalityChecker
  implements BiFunction<Vessel, Cell, Boolean>
{

  private final VesselComponent<? extends Gear> gear;
  private final Regulations<? super ExtendedFishingAction> regulations;
  private final BiFunction<
    ? super Vessel,
    ? super Cell,
    ? extends LocalDateTime
  > expectedFishingStart;

  /**
   * @param vessel      the vessel that would fish
   * @param fishingCell the cell it would fish at
   * @return whether fishing at {@code fishingCell} from the date-time the
   *         vessel is expected to start would be permitted for {@code vessel}
   */
  @Override
  public Boolean apply(final Vessel vessel, final Cell fishingCell) {
    return regulations.isPermitted(
      ExtendedFishingAction.expected(
        vessel,
        expectedFishingStart.apply(vessel, fishingCell),
        fishingCell,
        gear.of(vessel)
      )
    );
  }
}
