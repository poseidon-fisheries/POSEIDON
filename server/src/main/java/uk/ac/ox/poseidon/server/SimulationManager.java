/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.server;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.PropertyUtils;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.io.ScenarioLoader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class SimulationManager {
    private final Cache<UUID, Scenario> scenarios = CacheBuilder.newBuilder().build();
    private final Cache<UUID, Simulation> simulations = CacheBuilder.newBuilder().build();
    private final ScenarioLoader scenarioLoader;

    boolean containsScenario(@NonNull final UUID scenarioId) {
        return scenarios.asMap().containsKey(scenarioId);
    }

    boolean containsSimulation(@NonNull final UUID simulationId) {
        return simulations.asMap().containsKey(simulationId);
    }

    public void unloadScenario(@NonNull final UUID scenarioId) {
        scenarios.invalidate(scenarioId);
    }

    public UUID loadScenario(@NonNull final File scenarioFile) {
        return cache(scenarios, scenarioLoader.load(scenarioFile));
    }

    public UUID loadScenario(@NonNull final Class<? extends Scenario> scenarioClass) {
        final Scenario scenario;
        try {
            scenario = scenarioClass.getDeclaredConstructor().newInstance();
        } catch (
            final InstantiationException | IllegalAccessException | InvocationTargetException |
                  NoSuchMethodException e
        ) {
            throw new RuntimeException(e);
        }
        return cache(scenarios, scenario);
    }

    public UUID startSimulation(@NonNull final UUID scenarioId) {
        final Simulation simulation =
            Optional.ofNullable(scenarios.getIfPresent(scenarioId))
                .map(Scenario::newSimulation)
                .orElseThrow(() -> new ScenarioNotFoundException(scenarioId));
        simulation.start();
        return cache(simulations, simulation);
    }

    public void finishSimulation(@NonNull final UUID simulationId) {
        Optional
            .ofNullable(simulations.getIfPresent(simulationId))
            .ifPresent(Simulation::finish);
        simulations.invalidate(simulationId);
    }

    private <T> UUID cache(
        final Cache<UUID, T> cache,
        final T object
    ) {
        final UUID id = UUID.randomUUID();
        cache.put(id, object);
        return id;
    }

    public void setProperty(
        final UUID scenarioId,
        final String propertyName,
        final Object value
    ) {
        try {
            final Scenario scenario = getScenario(scenarioId);
            synchronized (scenario) {
                PropertyUtils.setProperty(scenario, propertyName, value);
            }
        } catch (
            final IllegalAccessException | InvocationTargetException | NoSuchMethodException e
        ) {
            throw new SetPropertyException(propertyName, e);
        }
    }

    private Scenario getScenario(final UUID scenarioId) {
        final Scenario scenario = scenarios.getIfPresent(scenarioId);
        if (scenario == null) {
            throw new ScenarioNotFoundException(scenarioId);
        }
        return scenario;
    }

    private Simulation getSimulation(final UUID simulationId) {
        final Simulation simulation = simulations.getIfPresent(simulationId);
        if (simulation == null) {
            throw new SimulationNotFoundException(simulationId);
        }
        return simulation;
    }

    public static class ScenarioNotFoundException extends RuntimeException {
        private ScenarioNotFoundException(final UUID scenarioId) {
            super("Scenario not found: " + scenarioId);
        }
    }

    public static class SimulationNotFoundException extends RuntimeException {
        private SimulationNotFoundException(final UUID simulationId) {
            super("Simulation not found: " + simulationId);
        }
    }

    public static class SetPropertyException extends RuntimeException {
        private SetPropertyException(
            final String propertyName,
            final Throwable cause
        ) {
            super("Unable to set property: " + propertyName, cause);
        }
    }
}
