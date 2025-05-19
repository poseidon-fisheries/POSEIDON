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

package uk.ac.ox.oxfish.maximization.generic;

import org.apache.commons.beanutils.PropertyUtils;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.lang.reflect.InvocationTargetException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ParameterAddress {
    private final String address;

    public ParameterAddress(final String address) {
        checkArgument(!checkNotNull(address).isEmpty());
        this.address = address;
    }

    public Object getValue(final Scenario scenario) {
        try {
            return PropertyUtils.getProperty(scenario, address);
        } catch (
            final IllegalAccessException | InvocationTargetException | NoSuchMethodException e
        ) {
            throw new RuntimeException(e);
        }
    }

    public void setValue(
        final Scenario scenario,
        final Object value
    ) {
        try {
            PropertyUtils.setProperty(scenario, address, value);
        } catch (
            final IllegalAccessException | InvocationTargetException | NoSuchMethodException e
        ) {
            throw new RuntimeException(e);
        }
    }

}
