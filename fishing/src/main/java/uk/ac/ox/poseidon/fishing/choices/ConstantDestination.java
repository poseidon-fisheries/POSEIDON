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

package uk.ac.ox.poseidon.fishing.choices;

import java.util.Optional;
import java.util.function.Function;
import lombok.NonNull;
import sim.util.Int2D;

/**
 * A destination that is the same fixed cell whatever the vessel, or none if
 * there is no cell.
 */
public class ConstantDestination implements Function<Object, Optional<Int2D>> {

  @NonNull
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private final Optional<Int2D> destination;

  /** @param destination the cell to always return; may be {@code null} */
  public ConstantDestination(final Int2D destination) {
    this.destination = Optional.ofNullable(destination);
  }

  @Override
  public Optional<Int2D> apply(final Object vessel) {
    return destination;
  }
}
