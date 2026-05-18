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

import lombok.*;
import org.apache.commons.beanutils.PropertyUtils;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;

import static uk.ac.ox.poseidon.core.scopes.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.time.Factories.dateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class Scenario {

    private Factory<Scope, LocalDateTime> startingDateTime;

    @Singular private Map<String, ? extends Factory<? super SimulationScope, ?>> components;

    public Scenario(
        final LocalDateTime startingDateTime,
        final Map<String, ? extends Factory<? super SimulationScope, ?>> components
    ) {
        this(dateTime(startingDateTime), components);
    }

    public Scenario(
        final LocalDate startingDate,
        final Map<String, ? extends Factory<? super SimulationScope, ?>> components
    ) {
        this(startingDate.atStartOfDay(), components);
    }

    public Simulation startNewSimulation() {
        return startNewSimulation(SimulationStartOptions.builder().build());
    }

    synchronized public Simulation startNewSimulation(
        final SimulationStartOptions options
    ) {
        if (options.getPropertyOverrides().isEmpty()) {
            return createSimulation(options);
        }
        final SequencedMap<String, Object> originalPropertyValues =
            getPropertyValues(options.getPropertyOverrides());
        try {
            setProperties(options.getPropertyOverrides());
            return createSimulation(options);
        } finally {
            restoreProperties(originalPropertyValues);
        }
    }

    private Simulation createSimulation(final SimulationStartOptions options) {
        final TemporalSchedule schedule =
            new TemporalSchedule(startingDateTime.get(GLOBAL_SCOPE));
        final Simulation simulation =
            new Simulation(options.getSeed(), schedule, options.getSimulationId());
        final SimulationScope simulationScope = new SimulationScope(simulation);
        simulation.start();
        simulation.components =
            getComponents()
                .values()
                .stream()
                .map(factory -> factory.get(simulationScope))
                .toList();
        return simulation;
    }

    private SequencedMap<String, Object> getPropertyValues(
        final Map<String, Object> properties
    ) {
        return properties
            .entrySet()
            .stream()
            .collect(
                LinkedHashMap::new,
                (values, entry) -> values.put(entry.getKey(), getProperty(entry.getKey())),
                Map::putAll
            );
    }

    private void restoreProperties(
        final SequencedMap<String, Object> propertyValues
    ) {
        propertyValues.reversed().forEach(this::setProperty);
    }

    synchronized void setProperties(final Map<String, Object> properties) {
        properties.forEach(this::setProperty);
    }

    synchronized Object getProperty(
        final String propertyName
    ) {
        try {
            return PropertyUtils.getProperty(this, propertyName);
        } catch (
            final InvocationTargetException | IllegalAccessException | NoSuchMethodException e
        ) {
            throw new RuntimeException(
                "Unable to read property " + propertyName,
                e
            );
        }
    }

    synchronized void setProperty(
        final String propertyName,
        final Object value
    ) {
        try {
            PropertyUtils.setProperty(this, propertyName, value);
        } catch (
            final InvocationTargetException | IllegalAccessException | NoSuchMethodException e
        ) {
            throw new RuntimeException(
                "Unable to set property " + propertyName + " to " + value,
                e
            );
        }
    }

    @SuppressWarnings("unchecked")
    public <C> Factory<? super SimulationScope, ? extends C> component(
        final String componentName
    ) {
        final Factory<? super SimulationScope, ?> factory = components.get(componentName);
        if (factory == null) {
            throw new IllegalArgumentException("Component not found: " + componentName);
        }
        return (Factory<? super SimulationScope, ? extends C>) factory;
    }

}
