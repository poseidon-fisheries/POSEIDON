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

package uk.ac.ox.poseidon.simulations.api;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;


public interface Scenario {
    Simulation newSimulation();

    default Object getParameterValue(final String parameterName) {
        final Map<String, Parameter> parameters = getParameters();
        checkArgument(
            parameters.containsKey(parameterName),
            "Parameter %s not found in scenario.",
            parameterName
        );
        return parameters.get(parameterName).getValue();
    }

    Map<String, Parameter> getParameters();

    default void setParameterValue(final String parameterName, final Object parameterValue) {
        final Map<String, Parameter> parameters = getParameters();
        checkArgument(
            parameters.containsKey(parameterName),
            "Parameter %s not found in scenario.",
            parameterName
        );
        parameters.get(parameterName).setValue(parameterValue);
    }
}
