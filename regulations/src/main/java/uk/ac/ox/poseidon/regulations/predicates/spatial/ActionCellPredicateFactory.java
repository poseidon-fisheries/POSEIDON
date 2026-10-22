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

package uk.ac.ox.poseidon.regulations.predicates.spatial;

import java.util.function.BiFunction;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.functions.WithoutContext;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.regulations.SpatialAction;

/**
 * A {@link RelativeScopeFactory} for an {@link ActionCellPredicate}. Built via
 * {@link Factories#actionCellPredicate}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class ActionCellPredicateFactory<S extends Scope>
  extends RelativeScopeFactory<S, BiFunction<Object, SpatialAction<?>, Boolean>>
  implements MappingFactory<S, Object, SpatialAction<?>, Boolean>
{

  private final Factory<? super S, ? extends ModelGrid> modelGrid;
  private final MappingFactory<
    ? super S,
    ? super Void,
    ? super Cell,
    Boolean
  > cellPredicate;

  @Override
  protected BiFunction<Object, SpatialAction<?>, Boolean> newInstance(
    final S scope
  ) {
    return new ActionCellPredicate(
      modelGrid.get(scope),
      WithoutContext.predicate(cellPredicate.get(scope))
    );
  }
}
