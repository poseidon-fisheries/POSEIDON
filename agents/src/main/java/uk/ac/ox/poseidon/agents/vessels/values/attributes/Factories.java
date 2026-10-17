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

/**
 * Factories for functions reading an attribute by name off anything that has
 * a vessel's attributes.
 */
public class Factories {

  private Factories() {}

  /**
   * @param name the name of the attribute to read
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link DoubleAttribute} reading the attribute named {@code name}
   */
  public static DoubleAttributeFactory doubleAttribute(final String name) {
    return new DoubleAttributeFactory(name);
  }

  /**
   * @param name the name of the attribute to read
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link StringAttribute} reading the attribute named {@code name}
   */
  public static StringAttributeFactory stringAttribute(final String name) {
    return new StringAttributeFactory(name);
  }
}
