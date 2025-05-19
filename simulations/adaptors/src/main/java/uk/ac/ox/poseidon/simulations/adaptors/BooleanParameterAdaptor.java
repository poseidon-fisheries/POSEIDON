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

package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.maximization.generic.ParameterAddress;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.parameters.BooleanParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedParameter;

import static com.google.common.base.Preconditions.checkArgument;

public class BooleanParameterAdaptor extends FixedParameterAdaptor<Boolean> {
    BooleanParameterAdaptor(
        final FixedParameter<Boolean> parameter,
        final String name,
        final Scenario scenario
    ) {
        super(parameter, name, scenario);
    }

    @Override
    public void setValue(final Object value) {
        checkArgument(
            value instanceof Boolean,
            "Value of parameter %s needs to be of type Boolean but got %s",
            getName(),
            value.getClass()
        );
        new ParameterAddress(getName()).setValue(scenario, new BooleanParameter((Boolean) value));
    }
}
