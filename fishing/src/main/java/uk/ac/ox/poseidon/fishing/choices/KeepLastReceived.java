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

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Remembers the incoming observation, regardless of when it was made.
 * An older report received later replaces a newer report.
 * @param <T> the observed and remembered type
 */
public class KeepLastReceived<T> implements UpdateRule<T, T> {

  @Override
  public T update(
    final Optional<T> remembered,
    final T observation,
    final LocalDateTime observationDateTime
  ) {
    return observation;
  }
}
