/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.common.core.temporal;

import java.time.temporal.TemporalAccessor;

/**
 * A {@link TemporalMap} that always returns the same object, no matter what date it is queried at.
 *
 * @param <T> The type of the stored object.
 */
public class ConstantTemporalMap<T> implements TemporalMap<T> {
    private final T object;

    public ConstantTemporalMap(final T object) {this.object = object;}

    @Override
    public T get(final TemporalAccessor temporal) {
        return object;
    }
}
