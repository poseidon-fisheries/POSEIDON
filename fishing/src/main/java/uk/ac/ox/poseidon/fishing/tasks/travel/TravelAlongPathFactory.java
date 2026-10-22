/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.fishing.tasks.TaskFactory;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.fishing.vessels.engines.Engine;
import uk.ac.ox.poseidon.fishing.vessels.tanks.FuelTank;
import uk.ac.ox.poseidon.geography.Destination;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.paths.PathFinder;

/**
 * A {@link TaskFactory} counterpart of {@link TravelAlongPath}, built via
 * {@link Factories#travelAlongPathTo}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class TravelAlongPathFactory extends TaskFactory<TravelAlongPath> {

  private final VesselComponent<? extends Engine> engine;
  private final VesselComponent<? extends FuelTank> fuelTank;
  private final Factory<
    ? super VesselComponentScope,
    ? extends PathFinder<Cell>
  > pathFinder;
  private final Factory<
    ? super VesselComponentScope,
    ? extends DistanceCalculator
  > distance;
  private final ValueFactory<
    ? super VesselComponentScope,
    ? super Vessel,
    ? extends Destination
  > destination;
  private final ValueFactory<
    ? super VesselComponentScope,
    ? super Vessel,
    EventManager
  > eventManager;
  private final ValueFactory<
    ? super VesselComponentScope,
    ? super Vessel,
    LocalDateTime
  > currentDateTime;

  @Override
  protected TravelAlongPath newInstance(final VesselComponentScope scope) {
    return new TravelAlongPath(
      engine,
      fuelTank,
      pathFinder.get(scope),
      distance.get(scope),
      destination.get(scope),
      eventManager.get(scope),
      currentDateTime.get(scope)
    );
  }
}
