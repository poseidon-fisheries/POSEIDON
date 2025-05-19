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

package uk.ac.ox.poseidon.simulations.adaptors;

import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.PropertyUtils;
import uk.ac.ox.poseidon.simulations.api.Parameter;

import java.lang.reflect.InvocationTargetException;

public class BeanParameter<T> implements Parameter {

    private final Object bean;
    private final String name;
    private final Class<T> parameterType;
    private final Converter converter;

    public BeanParameter(
        final Object bean,
        final String name,
        final Class<T> parameterType,
        final Converter converter
    ) {
        this.bean = bean;
        this.name = name;
        this.parameterType = parameterType;
        this.converter = converter;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getValue() {
        try {
            return PropertyUtils.getProperty(bean, name);
        } catch (
            final IllegalAccessException | InvocationTargetException | NoSuchMethodException e
        ) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setValue(final Object value) {
        try {
            PropertyUtils.setProperty(
                bean,
                name,
                converter.convert(parameterType, value)
            );
        } catch (
            final IllegalAccessException | InvocationTargetException | NoSuchMethodException e
        ) {
            throw new RuntimeException(e);
        }
    }
}
