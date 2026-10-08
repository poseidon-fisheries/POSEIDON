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

package uk.ac.ox.poseidon.core;

import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.scopes.Scope;

/**
 * A factory that builds its delegate's product once per scope of its own (once per simulation,
 * once per vessel...), whatever scope the delegate itself has. Subclasses fix that scope through
 * {@link #makeKey} and {@link #scopeClass}, as the {@code *ScopeFactory} classes do.
 * <p>
 * Each product is built by calling the delegate's own {@link AbstractFactory#newInstance}
 * directly, bypassing the delegate's cache: wrapping a global or simulation-scoped delegate does
 * give each scope its own instance, and leaves the instance the delegate shares elsewhere
 * untouched. If the delegate is not an {@link AbstractFactory} (e.g. a lambda in a test), it is
 * resolved with {@link Factory#get} instead.
 * <p>
 * <b>Only the delegate's own product is new: what it is built from is not.</b> The delegate
 * resolves its inputs as usual, at their own scopes, so a product that wraps a shared input (say,
 * a mutable object from a global factory) is a new wrapper around that same shared object. This is
 * the same rule as for any factory: a {@code VesselScopeFactory}'s inputs are not per vessel unless
 * they are themselves.
 *
 * @param <S> the scope this factory resolves against
 * @param <C> the type of object produced
 */
@NoArgsConstructor
public abstract class PerScopeFactory<S extends Scope, C> extends AbstractFactory<S, C> {

    /** @return the factory whose product is built once per scope */
    protected abstract Factory<? super S, ? extends C> getDelegate();

    /** Builds the delegate's product, bypassing the delegate's cache. */
    @Override
    protected final C newInstance(final S scope) {
        final Factory<? super S, ? extends C> delegate = getDelegate();
        return delegate instanceof final AbstractFactory<?, ?> abstractFactory
            ? newInstanceOf(abstractFactory, scope)
            : delegate.get(scope);
    }

    @SuppressWarnings("unchecked")
    private static <S extends Scope, C> C newInstanceOf(
        final AbstractFactory<?, ?> factory,
        final S scope
    ) {
        return ((AbstractFactory<? super S, ? extends C>) factory).newInstance(scope);
    }
}
