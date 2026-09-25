/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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
 * The framework's foundation: {@link uk.ac.ox.poseidon.core.Factory}, the declarative
 * configuration-to-runtime-object producer at the heart of the Factory/Scenario pattern (see
 * {@code docs/agents/architecture.md}), its {@link uk.ac.ox.poseidon.core.AbstractFactory} base
 * and {@code *ScopeFactory} caching variants
 * ({@link uk.ac.ox.poseidon.core.GlobalScopeFactory},
 * {@link uk.ac.ox.poseidon.core.SimulationScopeFactory},
 * {@link uk.ac.ox.poseidon.core.RelativeScopeFactory}), and
 * {@link uk.ac.ox.poseidon.core.Scenario}/{@link uk.ac.ox.poseidon.core.Simulation}, the
 * declarative description of a run and its resolved, runnable form. See
 * {@link uk.ac.ox.poseidon.core.MasonUtils} for MASON-specific helpers used throughout the
 * framework.
 */
package uk.ac.ox.poseidon.core;
