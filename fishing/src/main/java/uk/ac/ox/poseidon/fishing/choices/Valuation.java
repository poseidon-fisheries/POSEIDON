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
 * Converts a thing into worth in a vessel's circumstances, with higher values
 * preferred. It values only the thing supplied, independently of the option
 * for which an outcome was remembered. A cost has its own contract.
 *
 * @param <T> the type of thing valued
 */
@FunctionalInterface
public interface Valuation<T> {
  /**
   * Reads the vessel's circumstances once for one option-value visit.
   * The returned function is a snapshot, must not be retained beyond that
   * visit, and must not change model state. The model state it reads must not
   * change during the visit. Its answers do not depend on valuation order.
   *
   * @param vessel the vessel doing the valuing
   * @return the worth of each thing in these circumstances
   */
  ToDoubleFunction<T> by(Vessel vessel);
}
