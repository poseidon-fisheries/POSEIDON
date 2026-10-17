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

package uk.ac.ox.poseidon.agents.vessels.values.attributes;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.vessels.HasAttributes;

/**
 * Reads the attribute named {@code name} as a string: the vessel's current
 * value when given a vessel, the recorded one when given an action.
 */
@RequiredArgsConstructor
public class StringAttribute implements Function<HasAttributes, String> {

  @NonNull
  private final String name;

  /**
   * @throws IllegalArgumentException if {@code subject} has no attribute named
   *                                  {@link #name}
   */
  @Override
  public String apply(final HasAttributes subject) {
    final Object value = subject.getAttributes().get(name);
    checkArgument(value != null, "No attribute %s in %s", name, subject);
    return value.toString();
  }
}
