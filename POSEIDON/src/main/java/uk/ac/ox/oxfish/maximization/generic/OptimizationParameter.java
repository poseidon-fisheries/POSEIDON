/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2018-2025, University of Oxford.
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

import uk.ac.ox.oxfish.model.scenario.Scenario;

public interface OptimizationParameter {

    static void navigateAndSet(
        final Scenario scenario,
        final String address,
        final Object value
    ) {
        new ParameterAddress(address).setValue(scenario, value);
    }

    /**
     * number of parameters this object actually represents
     *
     * @return
     */
    int size();

    /**
     * consume the scenario and add the parameters
     *
     * @param scenario the scenario to modify
     * @param inputs   the numerical values of the parameters to set
     * @return
     */
    String parametrize(
        Scenario scenario,
        double[] inputs
    );

    String getName();
}
