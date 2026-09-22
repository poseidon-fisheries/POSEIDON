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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.scopes.Scope;

/**
 * A {@link Factory} that memoizes its produced object per scope key, per factory instance: the
 * same factory invocation, for the same scope key, always returns the same {@code C} instance —
 * this is how object identity/sharing is achieved across a scenario graph. Subclasses fix the
 * scope key (via {@link #makeKey}, usually inherited from a {@code *ScopeFactory} base rather than
 * overridden directly) and supply the actual construction logic (via {@link #newInstance}). Extend
 * this rather than implementing {@link Factory} directly unless you have a specific reason not to.
 *
 * @param <S> the scope this factory resolves against
 * @param <C> the type of object produced
 */
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode()
public abstract class AbstractFactory<S extends Scope, C> implements Factory<S, C> {

    // needs to be transient for SnakeYAML not to be confused
    // when there are no other properties to serialize and to
    // ensure it's not included in the equals and hashCode implementations
    private final transient LoadingCache<Object, Cache<Integer, C>> cache =
        Caffeine.newBuilder()
            .weakKeys()
            .build(object -> Caffeine.newBuilder().build());

    /**
     * Resolves and caches the produced object: looks up (creating if absent) the per-scope-key
     * cache via {@link #makeKey}, then within it looks up (creating via {@link #newInstance} if
     * absent) the entry for this factory instance's {@code hashCode()} — so distinct factory
     * instances with the same scope key still get distinct cached objects, and repeated calls
     * with an equal (by {@code hashCode()}) factory instance and scope key share one.
     *
     * @param scope the scope to resolve against, must not be null
     * @return the cached or newly-built object
     */
    @Override
    synchronized public final C get(@NonNull final S scope) {
        return cache
            .get(makeKey(scope))
            .get(hashCode(), key -> newInstance(scope));
    }

    /**
     * @param scope the scope being resolved against
     * @return the cache key identifying which objects this factory's output may be shared with
     * (e.g. the simulation instance, for per-simulation scope; a single constant, for global
     * scope)
     */
    protected abstract Object makeKey(S scope);

    /**
     * @param scope the scope being resolved against
     * @return a freshly built {@code C}, called at most once per distinct {@link #makeKey} result
     * and factory {@code hashCode()}
     */
    protected abstract C newInstance(S scope);

    /** @return the concrete {@link Scope} subclass this factory resolves against */
    protected abstract Class<S> scopeClass();

}
