/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.common.api;

import com.google.common.base.Supplier;

/**
 * A convenience class to make component factories that do not need to be passed the model state in order to construct
 * their object. In such cases, it is sufficient to implement the {@link #get()} method.
 *
 * @param <T> the type of object constructed by this factory.
 */
public abstract class ComponentSupplier<T> implements ComponentFactory<T>, Supplier<T> {
    @Override
    public T apply(final ModelState ignored) {
        return get();
    }
}
