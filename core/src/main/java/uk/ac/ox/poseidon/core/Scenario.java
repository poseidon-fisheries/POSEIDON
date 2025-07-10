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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class Scenario {

    private Date startingDateTime = new Date();

    private Map<String, ? extends Factory<?>> components = new HashMap<>();

    public Simulation newSimulation() {
        return newSimulation(System.currentTimeMillis(), UUID.randomUUID());
    }

    public Simulation newSimulation(
        final long seed,
        final UUID simulationId
    ) {
        return new Simulation(
            seed,
            new TemporalSchedule(startingDateTime.toInstant().atZone(UTC).toLocalDateTime()),
            this,
            simulationId
        );
    }

    @SuppressWarnings("unchecked")
    public <C> Factory<? extends C> component(
        final String componentName
    ) {
        final Factory<?> factory = components.get(componentName);
        if (factory == null) {
            throw new IllegalArgumentException("Component not found: " + componentName);
        }
        return (Factory<? extends C>) factory;
    }

}
