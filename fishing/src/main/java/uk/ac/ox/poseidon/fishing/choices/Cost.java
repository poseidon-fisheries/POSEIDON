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

import java.util.function.ToDoubleFunction;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * The cost of taking an option, with higher costs less desirable. Costs and
 * option values must use the same unit before subtraction.
 * @param <T> the type of option costed
 */
@FunctionalInterface
public interface Cost<T> {
  /**
   * Reads the vessel's circumstances for one option-value visit. The returned
   * function serves that visit only and must not be retained. Model state must
   * not change during the visit, and costing must not change model state.
   * @param vessel the vessel that would incur the cost
   * @return the cost of each option in those circumstances
   */
  ToDoubleFunction<T> forVessel(Vessel vessel);
}
