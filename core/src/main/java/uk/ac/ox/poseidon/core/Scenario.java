/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ox.poseidon.core.time.DateFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class Scenario {

    private Factory<? extends LocalDateTime> startingDateTime = new DateFactory();

    private Map<String, ? extends Factory<?>> components = new HashMap<>();

    public Simulation newSimulation() {
        return newSimulation(System.currentTimeMillis());
    }

    public Simulation newSimulation(final long seed) {
        return new Simulation(seed, startingDateTime.get(null), this);
    }

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
