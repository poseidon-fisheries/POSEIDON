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

package uk.ac.ox.poseidon.calibration;

/**
 * A parameter's permitted values and the mapping from a normalized search
 * coordinate to one of those values. The decoded numeric type is preserved
 * when applying the value to a scenario.
 */
public interface ParameterRange {
  /**
   * @param allele a finite search coordinate in the closed interval [0, 1]
   * @return the corresponding parameter value
   * @throws IllegalArgumentException if the coordinate is outside [0, 1]
   */
  Number decode(double allele);
}
