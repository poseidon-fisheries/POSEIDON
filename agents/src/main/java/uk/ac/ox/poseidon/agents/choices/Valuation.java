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

package uk.ac.ox.poseidon.agents.choices;

import uk.ac.ox.poseidon.agents.vessels.Vessel;

/**
 * Values an option for a vessel, given what is remembered about the option:
 * what the remembered catch would sell for at the vessel's home port, what it
 * would cost the vessel to travel there, or a combination of such valuations.
 * A valuation that does not depend on the option, or on what is remembered,
 * ignores it, so that it can be combined with others of the same shape.
 *
 * @param <O> the type of option valued
 * @param <M> the type of what is remembered about an option
 */
@FunctionalInterface
public interface Valuation<O, M> {
  /**
   * @param vessel       the vessel the option is valued for
   * @param option       the option valued
   * @param recollection what is remembered about the option
   * @return the value of {@code option} for {@code vessel}
   */
  double valueOf(Vessel vessel, O option, M recollection);
}
