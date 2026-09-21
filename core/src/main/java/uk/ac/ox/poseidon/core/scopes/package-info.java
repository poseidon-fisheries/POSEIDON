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

/**
 * The {@link uk.ac.ox.poseidon.core.scopes.Scope} hierarchy that marks the sharing/lifecycle
 * boundary a {@link uk.ac.ox.poseidon.core.Factory}-produced object belongs to: a single instance
 * shared globally across every simulation built from a scenario, or one scoped to a single
 * {@link uk.ac.ox.poseidon.core.Simulation} run. See the {@code *ScopeFactory} classes in
 * {@code uk.ac.ox.poseidon.core} ({@code GlobalScopeFactory}, {@code PerSimulationFactory},
 * {@code RelativeScopeFactory}, {@code SimulationScopeFactory}) for how factories pick a scope.
 */
package uk.ac.ox.poseidon.core.scopes;
