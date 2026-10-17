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

package uk.ac.ox.poseidon.geography.predicates;

import java.util.function.BiFunction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.Envelope;

/**
 * A yes/no mapping, whatever the context, testing whether a {@link Coordinate}
 * falls within a fixed {@link Envelope}. Built via
 * {@link Factories#inRectangularArea(uk.ac.ox.poseidon.core.Factory)} in this
 * package.
 */
@Getter
@RequiredArgsConstructor
public class InRectangularArea
  implements BiFunction<Object, Coordinate, Boolean>
{

  private final Envelope envelope;

  /**
   * @return whether {@code coordinate} falls within this predicate's envelope
   */
  @Override
  public Boolean apply(final Object context, final Coordinate coordinate) {
    return envelope.contains(coordinate);
  }
}
