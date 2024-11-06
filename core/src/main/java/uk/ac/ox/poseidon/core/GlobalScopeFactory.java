/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.core;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class GlobalScopeFactory<C> extends CachingFactory<C> {

    // needs to be transient for SnakeYAML not to be confused
    // when there are no other properties to serialize
    private final transient Cache<List<Object>, C> cache =
        CacheBuilder.newBuilder().build();

    @Override
    public final C get(final Simulation simulation) {
        try {
            return cache.get(makeKey(simulation), () -> newInstance(simulation));
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
