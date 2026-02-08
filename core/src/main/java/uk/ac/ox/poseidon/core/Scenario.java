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
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class Scenario {

    private Date startingDateTime;

    @Singular private Map<String, ? extends Factory<? super SimulationScope, ?>> components;

    public Scenario(
        final LocalDateTime startingDateTime,
        final Map<String, ? extends Factory<? super SimulationScope, ?>> components
    ) {
        this(Date.from(startingDateTime.atZone(UTC).toInstant()), components);
    }

    public Scenario(
        final LocalDate startingDate,
        final Map<String, ? extends Factory<? super SimulationScope, ?>> components
    ) {
        this(startingDate.atStartOfDay(), components);
    }

    public Simulation startNewSimulation() {
        return startNewSimulation(System.currentTimeMillis(), UUID.randomUUID());
    }

    public Simulation startNewSimulation(final UUID simulationId) {
        return startNewSimulation(System.currentTimeMillis(), simulationId);
    }

    synchronized public Simulation startNewSimulation(
        final long seed,
        final UUID simulationId
    ) {
        final LocalDateTime localDateTime =
            startingDateTime.toInstant().atZone(UTC).toLocalDateTime();
        final TemporalSchedule schedule = new TemporalSchedule(localDateTime);
        final Simulation simulation = new Simulation(seed, schedule, simulationId);
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

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    public static class ScenarioBuilder {

        private Date startingDateTime;

        public ScenarioBuilder startingDateTime(final Date startingDateTime) {
            this.startingDateTime = startingDateTime;
            return this;
        }

        public ScenarioBuilder startingDateTime(final LocalDate startingDate) {
            return startingDateTime(startingDate.atStartOfDay());
        }

        public ScenarioBuilder startingDateTime(final LocalDateTime startingDateTime) {
            return startingDateTime(startingDateTime.atZone(UTC));
        }

        public ScenarioBuilder startingDateTime(final ZonedDateTime zonedDateTime) {
            return startingDateTime(Date.from(zonedDateTime.toInstant()));
        }

    }

}
