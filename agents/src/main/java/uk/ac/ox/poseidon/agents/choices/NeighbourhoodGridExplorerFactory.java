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

package uk.ac.ox.poseidon.agents.choices;

import ec.util.MersenneTwisterFast;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

/**
 * A {@link VesselScopeFactory} counterpart of {@link NeighbourhoodCellPicker},
 * built via {@link Factories#neighbourhoodGridExplorer}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class NeighbourhoodGridExplorerFactory
  extends VesselScopeFactory<NeighbourhoodCellPicker>
{

  private final Factory<
    ? super VesselScope,
    ? extends GridPathFinder
  > pathFinder;
  private final Factory<
    ? super VesselScope,
    ? extends Predicate<? super Int2D>
  > cellPredicate;
  private final Factory<
    ? super VesselScope,
    ? extends IntSupplier
  > neighbourhoodSizeSupplier;
  private final int maximumWidening;
  private final Factory<
    ? super VesselScope,
    ? extends Supplier<Int2D>
  > searchCentre;
  private final Factory<
    ? super VesselScope,
    ? extends MersenneTwisterFast
  > randomNumberGenerator;

  @Override
  protected NeighbourhoodCellPicker newInstance(final VesselScope scope) {
    return new NeighbourhoodCellPicker(
      scope.getVessel(),
      pathFinder.get(scope),
      cellPredicate.get(scope),
      neighbourhoodSizeSupplier.get(scope),
      maximumWidening,
      searchCentre.get(scope),
      randomNumberGenerator.get(scope)
    );
  }
}
