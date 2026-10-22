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

package uk.ac.ox.poseidon.fishing.travel;

import com.google.common.collect.ImmutableList;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.joda.money.Money;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.geography.grids.Cell;

/**
 * A {@link RelativeScopeFactory} counterpart of {@link TravelCost}, built via
 * {@link Factories#travelCost(MappingFactory, ValueFactory...)}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class TravelCostFactory<S extends Scope> extends RelativeScopeFactory<
  S,
  TravelCost
> {

  private final MappingFactory<
    ? super S,
    ? super Vessel,
    ? super Cell,
    ? extends Route
  > routeFunction;

  @Singular
  private final ImmutableList<
    ValueFactory<
      ? super S,
      ? super Vessel,
      ? extends Function<? super Route, ? extends Money>
    >
  > routeCosts;

  @Override
  protected TravelCost newInstance(final S scope) {
    return new TravelCost(
      routeFunction.get(scope),
      routeCosts
        .stream()
        .map(routeCost -> routeCost.get(scope))
        .toList()
    );
  }
}
