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

import static uk.ac.ox.poseidon.core.providers.temporal.Factories.currentDateTime;

import java.time.Duration;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.fishing.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.engines.Engine;
import uk.ac.ox.poseidon.fishing.vessels.gears.Gear;
import uk.ac.ox.poseidon.geography.paths.PathLengthCalculator;
import uk.ac.ox.poseidon.regulations.Regulations;

/**
 * Factories for predicates over candidate fishing cells, built on the
 * {@link uk.ac.ox.poseidon.regulations} model.
 */
public final class Factories {

  private Factories() {}

  /**
   * @param gear                 the gear the vessel would fish with
   * @param engine               the engine whose speed gives the time to get
   *                             there
   * @param regulations          the regulations a candidate fishing action must
   *                             be permitted by
   * @param pathLengthCalculator measures the path from the vessel's current
   *                             cell to the candidate
   * @param fishingDuration      the typical duration of a fishing action,
   *                             which the candidate action lasts (see
   *                             {@link FishingCellLegalityChecker})
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link FishingCellLegalityChecker}
   * @see FishingCellLegalityCheckerFactory
   */
  public static <S extends SimulationScope> FishingCellLegalityCheckerFactory<S> fishingCellLegalityChecker(
    final VesselComponent<? extends Gear> gear,
    final VesselComponent<? extends Engine> engine,
    final Factory<
      ? super S,
      ? extends Regulations<? super ExtendedFishingAction>
    > regulations,
    final Factory<
      ? super S,
      ? extends PathLengthCalculator
    > pathLengthCalculator,
    final Factory<? super S, ? extends Duration> fishingDuration
  ) {
    return new FishingCellLegalityCheckerFactory<>(
      gear,
      engine,
      regulations,
      pathLengthCalculator,
      fishingDuration,
      currentDateTime()
    );
  }
}
