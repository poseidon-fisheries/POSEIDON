/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.maximization.generic;

import uk.ac.ox.oxfish.model.scenario.Scenario;

//for testing only!
public class FixedOptimizationParameter implements OptimizationParameter {


    private final double realValue;

    private final String address;

    public FixedOptimizationParameter(double realValue,
                                      String address) {
        this.realValue = realValue;
        this.address = address;
    }

    /**
     * number of parameters this object actually represents
     *
     * @return
     */
    @Override
    public int size() {
        return 1;
    }

    /**
     * consume the scenario and add the parameters
     *
     * @param scenario the scenario to modify
     * @param inputs   the numerical values of the parameters to set
     */
    @Override
    public double parametrize(Scenario scenario, double[] inputs) {
        SimpleOptimizationParameter.quickParametrize(scenario,
                                                     realValue,
                                                     address);
        return realValue;

    }
}
