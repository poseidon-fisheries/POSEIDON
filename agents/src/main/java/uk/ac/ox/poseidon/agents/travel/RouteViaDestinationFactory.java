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

package uk.ac.ox.poseidon.agents.travel;

import java.util.function.BiFunction;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselComponent;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

/**
 * A {@link VesselScopeFactory} counterpart of {@link RouteViaDestination},
 * built via {@link Factories#routeViaDestination(Factory, Factory)}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class RouteViaDestinationFactory
  extends VesselScopeFactory<BiFunction<Vessel, Int2D, Route>>
  implements MappingFactory<VesselScope, Vessel, Int2D, Route>
{

  private final VesselComponent<? extends Engine> engine;
  private final Factory<
    ? super VesselScope,
    ? extends GridPathFinder
  > pathFinder;
  private final Factory<
    ? super VesselScope,
    ? extends DistanceCalculator
  > distance;

  @Override
  protected BiFunction<Vessel, Int2D, Route> newInstance(
    final VesselScope scope
  ) {
    return new RouteViaDestination(
      engine,
      pathFinder.get(scope),
      distance.get(scope)
    );
  }
}
