/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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
import lombok.Setter;

import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.function.Supplier;

import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.toMap;

@Getter
@Setter
@AllArgsConstructor
public abstract class ScenarioSupplier implements Supplier<Scenario> {

    private Date startingDate;

    public ScenarioSupplier(final LocalDate startingDate) {
        this(startingDate.atStartOfDay());
    }

    public ScenarioSupplier(final LocalDateTime startingDateTime) {
        this(startingDateTime.atZone(UTC));
    }

    public ScenarioSupplier(final ZonedDateTime zonedDateTime) {
        this(Date.from(zonedDateTime.toInstant()));
    }

    private Map<String, Factory<?>> extractComponents() {
        try {
            return Arrays
                .stream(Introspector.getBeanInfo(this.getClass()).getPropertyDescriptors())
                .filter(propertyDescriptor ->
                    !propertyDescriptor.getName().equals("startingDateTime") &&
                        propertyDescriptor.getReadMethod() != null &&
                        Factory.class.isAssignableFrom(
                            propertyDescriptor.getReadMethod().getReturnType()
                        )
                )
                .collect(toMap(
                    FeatureDescriptor::getName,
                    propertyDescriptor -> {
                        try {
                            return (Factory<?>) propertyDescriptor.getReadMethod().invoke(this);
                        } catch (final IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                ));
        } catch (final IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Scenario get() {
        return new Scenario(
            startingDate,
            extractComponents()
        );
    }
}
