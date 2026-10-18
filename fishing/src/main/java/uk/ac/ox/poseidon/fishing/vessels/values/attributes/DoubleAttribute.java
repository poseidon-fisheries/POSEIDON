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

package uk.ac.ox.poseidon.fishing.vessels.values.attributes;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.fishing.vessels.HasAttributes;

/**
 * Reads the attribute named {@code name} as a {@link Double}: the vessel's
 * current value when given a vessel, the recorded one when given an action. A
 * {@link Number} gives its {@link Number#doubleValue()}; any other value is
 * parsed from its trimmed string form.
 */
@RequiredArgsConstructor
public class DoubleAttribute implements Function<HasAttributes, Double> {

  @NonNull
  private final String name;

  /**
   * @throws IllegalArgumentException if {@code subject} has no attribute named
   *                                  {@link #name}
   * @throws NumberFormatException    if the value is not a number and does not
   *                                  parse as one
   */
  @Override
  public Double apply(final HasAttributes subject) {
    final Object value = subject.getAttributes().get(name);
    checkArgument(value != null, "No attribute %s in %s", name, subject);
    return value instanceof final Number number
      ? number.doubleValue()
      : Double.valueOf(value.toString().trim());
  }
}
