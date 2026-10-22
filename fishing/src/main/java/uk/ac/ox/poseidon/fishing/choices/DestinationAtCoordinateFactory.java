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

package uk.ac.ox.poseidon.fishing.choices;

import java.util.Optional;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

/**
 * A {@link RelativeScopeFactory} counterpart of {@link DestinationAtCoordinate},
 * built via {@link Factories#destinationAt}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class DestinationAtCoordinateFactory<S extends Scope, C>
  extends RelativeScopeFactory<S, Function<C, Optional<Cell>>>
  implements ValueFactory<S, C, Optional<Cell>>
{

  private final Factory<? super S, ? extends ModelGrid> modelGrid;
  private final ValueFactory<? super S, ? super C, ? extends Double> longitude;
  private final ValueFactory<? super S, ? super C, ? extends Double> latitude;

  @Override
  protected Function<C, Optional<Cell>> newInstance(final S scope) {
    return new DestinationAtCoordinate<>(
      modelGrid.get(scope),
      longitude.get(scope),
      latitude.get(scope)
    );
  }
}
