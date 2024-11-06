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

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.time.DateFactory;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
public class Scenario {

    private Factory<? extends LocalDateTime> startingDateTime = new DateFactory();

    public Scenario(final Factory<? extends LocalDateTime> startingDateTime) {
        this.startingDateTime = startingDateTime;
    }

    public Simulation newSimulation() {
        return newSimulation(System.currentTimeMillis());
    }

    public Simulation newSimulation(final long seed) {
        return new Simulation(seed, startingDateTime.get(null), this);
    }

    public Stream<? extends Factory<?>> extractFactories() {
        try {
            return Arrays
                .stream(Introspector.getBeanInfo(this.getClass()).getPropertyDescriptors())
                .filter(propertyDescriptor ->
                    !propertyDescriptor.getName().equals("startingDateTime")
                )
                .map(PropertyDescriptor::getReadMethod)
                .filter(Objects::nonNull)
                .filter(readMethod -> Factory.class.isAssignableFrom(readMethod.getReturnType()))
                .map(readMethod -> {
                    try {
                        return readMethod.invoke(this);
                    } catch (final IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(o -> (Factory<?>) o);
        } catch (final IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

}
