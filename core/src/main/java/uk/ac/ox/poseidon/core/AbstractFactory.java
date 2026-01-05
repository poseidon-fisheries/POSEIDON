/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.core;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.scopes.Scope;

import static com.google.common.cache.CacheLoader.from;

@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode()
public abstract class AbstractFactory<S extends Scope, C> implements Factory<S, C> {

    // needs to be transient for SnakeYAML not to be confused
    // when there are no other properties to serialize and to
    // ensure it's not included in the equals and hashCode implementations
    private final transient LoadingCache<S, LoadingCache<Integer, C>> cache =
        CacheBuilder.newBuilder()
            .weakKeys()
            .build(from(scope ->
                CacheBuilder.newBuilder()
                    .build(from(() -> newInstance(scope)))
            ));

    @Override
    public final C get(final S scope) {
        return cache
            .getUnchecked(scope)
            .getUnchecked(hashCode());
    }

    protected abstract C newInstance(S scope);

}
