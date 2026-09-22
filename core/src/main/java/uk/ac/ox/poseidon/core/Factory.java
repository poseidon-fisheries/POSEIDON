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

import uk.ac.ox.poseidon.core.scopes.Scope;

/**
 * The core declarative-configuration-to-runtime-object producer of the Factory/Scenario pattern
 * (see {@code docs/agents/architecture.md}): resolves to a {@code C} given a {@code Scope}. Most
 * simulation components are expressed as a {@link Factory} implementation (typically extending
 * {@link AbstractFactory}) plus a plain value/config class, so scenarios can be built either
 * programmatically or deserialized from YAML. Scenario-building code composes {@code Factory}-
 * returning static helpers only; never call {@link #get} directly in scenario-building code —
 * resolving the graph is the framework's job at simulation-start time.
 *
 * @param <S> the scope this factory resolves against
 * @param <C> the type of object produced
 */
public interface Factory<S extends Scope, C> {
    /**
     * @param scope the scope to resolve this factory against
     * @return the resolved object, of type {@code C}
     */
    C get(S scope);
}
