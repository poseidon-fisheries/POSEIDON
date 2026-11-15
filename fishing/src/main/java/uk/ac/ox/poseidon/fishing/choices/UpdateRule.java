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
 * Folds an observation and its time into what is remembered under one key.
 * The first observation receives an empty remembered value too.
 * @param <T> the observation type
 * @param <M> the remembered type
 */
@FunctionalInterface
public interface UpdateRule<T, M> {
  /**
   * @param remembered what was remembered, or empty for a new key
   * @param observation what was observed
   * @param observationDateTime when the observation was made
   * @return what to remember; must not be null
   */
  M update(
    Optional<M> remembered,
    T observation,
    LocalDateTime observationDateTime
  );
}
