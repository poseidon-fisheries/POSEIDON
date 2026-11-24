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

package uk.ac.ox.poseidon.fishing.regulations.predicates;

import java.time.LocalDateTime;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.fishing.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.gears.Gear;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.regulations.Regulations;

/**
 * Factories for predicates over candidate fishing cells, built on the
 * {@link uk.ac.ox.poseidon.regulations} model.
 */
public final class Factories {

  private Factories() {}

  /**
   * @param gear                 the gear the vessel would fish with
   * @param regulations          the regulations a candidate fishing action must
   *                             be permitted by
   * @param expectedFishingStart gives the date-time a vessel would start
   *                             fishing at a candidate cell, such as its
   *                             arrival there plus any wait for fishing hours
   * @return a {@link MappingFactory} for a {@link FishingCellLegalityChecker}
   * @see FishingCellLegalityCheckerFactory
   */
  public static <S extends Scope> FishingCellLegalityCheckerFactory<S> fishingCellLegalityChecker(
    final VesselComponent<? extends Gear> gear,
    final Factory<
      ? super S,
      ? extends Regulations<? super ExtendedFishingAction>
    > regulations,
    final MappingFactory<
      ? super S,
      ? super Vessel,
      ? super Cell,
      ? extends LocalDateTime
    > expectedFishingStart
  ) {
    return new FishingCellLegalityCheckerFactory<>(
      gear,
      regulations,
      expectedFishingStart
    );
  }
}
