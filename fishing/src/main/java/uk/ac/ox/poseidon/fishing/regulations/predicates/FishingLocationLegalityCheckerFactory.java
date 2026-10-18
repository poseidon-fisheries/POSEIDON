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
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.fishing.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.VesselScope;
import uk.ac.ox.poseidon.fishing.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.fishing.vessels.engines.Engine;
import uk.ac.ox.poseidon.fishing.vessels.gears.Gear;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;
import uk.ac.ox.poseidon.regulations.Regulations;

/**
 * A {@link VesselScopeFactory} for a {@link FishingLocationLegalityChecker}.
 * Built via {@link Factories#fishingLocationLegalityChecker}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class FishingLocationLegalityCheckerFactory
  extends VesselScopeFactory<BiFunction<Vessel, Int2D, Boolean>>
  implements MappingFactory<VesselScope, Vessel, Int2D, Boolean>
{

  private final VesselComponent<? extends Gear> gear;
  private final VesselComponent<? extends Engine> engine;

  private final Factory<
    ? super VesselScope,
    ? extends Regulations<? super ExtendedFishingAction>
  > regulations;

  private final Factory<
    ? super VesselScope,
    ? extends GridPathFinder
  > pathFinder;
  private final Factory<
    ? super VesselScope,
    ? extends DistanceCalculator
  > distance;
  private final Factory<
    ? super VesselScope,
    ? extends Duration
  > fishingDuration;
  private final ValueFactory<
    ? super VesselScope,
    ? super Vessel,
    LocalDateTime
  > currentDateTime;

  @Override
  protected BiFunction<Vessel, Int2D, Boolean> newInstance(
    final VesselScope scope
  ) {
    return new FishingLocationLegalityChecker(
      gear,
      engine,
      regulations.get(scope),
      pathFinder.get(scope),
      distance.get(scope),
      fishingDuration.get(scope),
      currentDateTime.get(scope)
    );
  }
}
