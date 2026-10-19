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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.BiFunction;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.fishing.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.engines.Engine;
import uk.ac.ox.poseidon.fishing.vessels.gears.Gear;
import uk.ac.ox.poseidon.geography.paths.PathLengthCalculator;
import uk.ac.ox.poseidon.regulations.Regulations;

/**
 * A {@link RelativeScopeFactory} for a {@link FishingCellLegalityChecker}.
 * Built via {@link Factories#fishingCellLegalityChecker}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class FishingCellLegalityCheckerFactory<S extends Scope>
  extends RelativeScopeFactory<S, BiFunction<Vessel, Int2D, Boolean>>
  implements MappingFactory<S, Vessel, Int2D, Boolean>
{

  private final VesselComponent<? extends Gear> gear;
  private final VesselComponent<? extends Engine> engine;

  private final Factory<
    ? super S,
    ? extends Regulations<? super ExtendedFishingAction>
  > regulations;

  private final Factory<
    ? super S,
    ? extends PathLengthCalculator
  > pathLengthCalculator;
  private final Factory<? super S, ? extends Duration> fishingDuration;
  private final ValueFactory<
    ? super S,
    ? super Vessel,
    LocalDateTime
  > currentDateTime;

  @Override
  protected BiFunction<Vessel, Int2D, Boolean> newInstance(final S scope) {
    return new FishingCellLegalityChecker(
      gear,
      engine,
      regulations.get(scope),
      pathLengthCalculator.get(scope),
      fishingDuration.get(scope),
      currentDateTime.get(scope)
    );
  }
}
