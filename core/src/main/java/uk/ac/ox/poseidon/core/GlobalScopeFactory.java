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

import uk.ac.ox.poseidon.core.scopes.GlobalScope;

import java.util.function.Supplier;

import static uk.ac.ox.poseidon.core.scopes.GlobalScope.GLOBAL_SCOPE;

public abstract class GlobalScopeFactory<C>
    extends AbstractFactory<GlobalScope, C>
    implements Supplier<C> {

    protected GlobalScopeFactory() {
        super(GlobalScope.class);
    }

    @Override
    public final C get() {
        return super.get(GLOBAL_SCOPE);
    }

    protected abstract C newInstance();

    @Override
    protected final C newInstance(final GlobalScope scope) {
        return newInstance();
    }
}
