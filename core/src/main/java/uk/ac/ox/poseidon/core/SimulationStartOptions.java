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

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.util.Map;
import java.util.UUID;

/**
 * Per-run overrides passed to {@link Scenario#startNewSimulation(SimulationStartOptions)}: an id
 * and RNG seed for the run, bean-property overrides temporarily applied to the {@link Scenario}
 * for the duration of the build (dotted property paths, via {@code commons-beanutils}), and extra
 * components to resolve alongside the scenario's own. Used heavily by calibration, which needs to
 * vary parameters run-to-run without mutating the original {@link Scenario}.
 */
@Getter
@Builder
public final class SimulationStartOptions {

    @Builder.Default
    private final UUID simulationId = UUID.randomUUID();

    @Builder.Default
    private final long seed = System.currentTimeMillis();

    @Singular private final Map<String, Object> propertyOverrides;

    @Singular private Map<String, ? extends Factory<? super SimulationScope, ?>> extraComponents;
}
