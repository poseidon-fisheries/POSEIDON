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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.fishing.trips.FishingTripEndEvent;
import uk.ac.ox.poseidon.geography.grids.Cell;

/**
 * A {@link GlobalScopeFactory} counterpart of {@link DestinationCell},
 * built via {@link Factories#destinationCell()}.
 */
@Getter
@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class DestinationCellFactory
  extends GlobalScopeFactory<Function<FishingTripEndEvent, Optional<Cell>>>
  implements ValueFactory<Scope, FishingTripEndEvent, Optional<Cell>>
{

  @Override
  protected Function<FishingTripEndEvent, Optional<Cell>> newInstance(
    final Scope scope
  ) {
    return new DestinationCell();
  }
}
