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

package uk.ac.ox.poseidon.core.providers;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A supplier of T that can also be used in contexts requiring a function, in which case it ignores
 * the argument passed to `apply` and just returns the object it would have supplied anyway.
 *
 * @param <T> the type of object returned by the provider
 */
public interface Provider<T> extends Supplier<T>, Function<Object, T> {
    @Override
    default T apply(final Object ignored) {return get();}
}
