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

package uk.ac.ox.poseidon.agents.tasks.travel;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.agents.tasks.TaskFactory;
import uk.ac.ox.poseidon.agents.vessels.VesselComponent;
import uk.ac.ox.poseidon.agents.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.agents.vessels.tanks.FuelTank;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;

/**
 * A {@link TaskFactory} counterpart of {@link TravelDirectly}, built via
 * {@link Factories#travelDirectly}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class TravelDirectlyFactory extends TaskFactory<TravelDirectly> {

  private final VesselComponent<? extends Engine> engine;
  private final VesselComponent<? extends FuelTank> fuelTank;
  private final Factory<
    ? super VesselComponentScope,
    ? extends DistanceCalculator
  > distance;

  @Override
  protected TravelDirectly newInstance(final VesselComponentScope scope) {
    return new TravelDirectly(engine, fuelTank, distance.get(scope));
  }
}
