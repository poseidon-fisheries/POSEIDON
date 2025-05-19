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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

/**
 * returns a uniform double parameter centered on average and with random spread.
 */
public class UniformWithSpreadOptimizationParameter implements OptimizationParameter {

    private String addressToModify = "literPerKilometer";

    private double average = 650;

    private double minSpread = 10;

    private double maxSpread = 200;

    @Override
    public int size() {
        return 1;
    }

    @Override
    public String parametrize(
        Scenario scenario,
        double[] inputs
    ) {
        Preconditions.checkArgument(maxSpread >= minSpread, "invalid bounds " + addressToModify);
        Preconditions.checkArgument(inputs.length == 1);

        double actualSpread = SimpleOptimizationParameter.computeNumericValueFromEVABounds(
            inputs[0], minSpread, maxSpread, true
        );
        double actualMinimum = average - actualSpread;
        double actualMaximum = average + actualSpread;

        try {
            OptimizationParameter.navigateAndSet(
                scenario, addressToModify, new UniformDoubleParameter(
                    actualMinimum,
                    actualMaximum
                )

            );
        } catch (Exception e) {

            throw new RuntimeException(e);

        }

        return String.valueOf(actualSpread);
    }

    @Override
    public String getName() {
        return addressToModify;
    }

    public String getAddressToModify() {
        return addressToModify;
    }

    public void setAddressToModify(String addressToModify) {
        this.addressToModify = addressToModify;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public double getMinSpread() {
        return minSpread;
    }

    public void setMinSpread(double minSpread) {
        this.minSpread = minSpread;
    }

    public double getMaxSpread() {
        return maxSpread;
    }

    public void setMaxSpread(double maxSpread) {
        this.maxSpread = maxSpread;
    }
}
