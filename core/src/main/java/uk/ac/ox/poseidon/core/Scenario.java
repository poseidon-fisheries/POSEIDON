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
import java.util.stream.Stream;

import static uk.ac.ox.poseidon.core.scopes.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.time.Factories.dateTime;

/**
 * An immutable-by-convention (Lombok {@link Builder}), declarative bag of named component
 * {@link Factory} instances plus a starting date/time — the serializable, YAML-loadable
 * description of a simulation to run. Every field must remain serializable to YAML; don't
 * introduce fields/types that can't survive a SnakeYAML round-trip. {@link #startNewSimulation()}
 * is the entry point: it resolves every component factory against a fresh {@link SimulationScope}
 * to build a runnable {@link Simulation}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class Scenario {

    private Factory<Scope, LocalDateTime> startingDateTime;

    @Singular private Map<String, ? extends Factory<? super SimulationScope, ?>> components;

    /**
     * @param startingDateTime the simulation's starting date-time
     * @param components       the named component factories making up the scenario
     */
    public Scenario(
        final LocalDateTime startingDateTime,
        final Map<String, ? extends Factory<? super SimulationScope, ?>> components
    ) {
        this(dateTime(startingDateTime), components);
    }

    /**
     * @param startingDate the simulation's starting date, at midnight
     * @param components   the named component factories making up the scenario
     */
    public Scenario(
        final LocalDate startingDate,
        final Map<String, ? extends Factory<? super SimulationScope, ?>> components
    ) {
        this(startingDate.atStartOfDay(), components);
    }

    /**
     * Equivalent to {@link #startNewSimulation(SimulationStartOptions)} with default options (a
     * random seed, no property overrides, no extra components).
     *
     * @return the started {@link Simulation}
     */
    public Simulation startNewSimulation() {
        return startNewSimulation(SimulationStartOptions.builder().build());
    }

    /**
     * Builds and starts a new {@link Simulation} by resolving every component factory (plus
     * {@code options}' extra components) against a fresh {@link SimulationScope}.
     * <p>
     * If {@code options} carries property overrides, this method temporarily mutates {@code this}
     * {@link Scenario}'s bean properties (via reflection, dotted property paths) to the override
     * values for the duration of the build, then restores the original values in a
     * {@code finally} block before returning — even though the method is {@code synchronized} on
     * {@code this}, that only serializes concurrent calls against the same {@link Scenario}
     * instance; any other code reading this scenario's properties concurrently could observe the
     * temporarily-overridden values. This is how calibration varies parameters run-to-run without
     * permanently mutating the original {@link Scenario}.
     *
     * @param options seed, property overrides, and extra components for this run
     * @return the started {@link Simulation}
     */
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
            Stream
                .<Factory<? super SimulationScope, ?>>concat(
                    getComponents().values().stream(),
                    options.getExtraComponents().values().stream()
                )
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

    /**
     * @param componentName the component's name, as registered in {@link #components}
     * @return the named component's factory, unchecked-cast to a {@code Factory<? super
     * SimulationScope, ? extends C>}
     * @throws IllegalArgumentException if no component is registered under that name
     */
    @SuppressWarnings("unchecked")
    public <C> Factory<? super SimulationScope, ? extends C> component(
        final String componentName
    ) {
        return (Factory<? super SimulationScope, ? extends C>) component(
            componentName,
            Factory.class
        );
    }

    /**
     * @param componentName the component's name, as registered in {@link #components}
     * @param factoryClass  the expected factory type
     * @return the named component's factory, cast to {@code factoryClass}
     * @throws IllegalArgumentException if no component is registered under that name, or if it's
     *                                  not an instance of {@code factoryClass}
     */
    public <F extends Factory<?, ?>> F component(
        final String componentName,
        final Class<? extends F> factoryClass
    ) {
        final var factory = components.get(componentName);
        if (factory == null) {
            throw new IllegalArgumentException("Component not found: " + componentName);
        }
        if (!factoryClass.isInstance(factory)) {
            throw new IllegalArgumentException("Factory is not of type " + factoryClass.getName());
        }
        return factoryClass.cast(factory);
    }

}
