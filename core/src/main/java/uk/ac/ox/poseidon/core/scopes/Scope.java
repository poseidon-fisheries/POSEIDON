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

package uk.ac.ox.poseidon.core.scopes;

/**
 * Marks the sharing/lifecycle boundary a {@link uk.ac.ox.poseidon.core.Factory}-produced object
 * belongs to. The base class itself represents the global scope, shared by every
 * {@code AbstractFactory} invocation regardless of the simulation it's built for; subclasses such
 * as {@link SimulationScope} narrow that boundary to a single simulation run.
 */
public class Scope {

    /**
     * The single global scope instance, used by factories whose output is shared across every
     * simulation built from a scenario (see {@code GlobalScopeFactory}).
     */
    @SuppressWarnings("InstantiationOfUtilityClass")
    public static final Scope GLOBAL_SCOPE = new Scope();

    Scope() {
    }

}
