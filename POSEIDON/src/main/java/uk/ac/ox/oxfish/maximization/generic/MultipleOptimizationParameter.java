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

import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.util.LinkedList;
import java.util.List;

import static uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter.quickParametrize;
import static uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter.quickParametrizeRawNumber;

/**
 * like simple optimization parameter, but puts the same number at multiple addresses
 */
public class MultipleOptimizationParameter implements OptimizationParameter {


    private List<String> addressesToModify = new LinkedList<>(); //"literPerKilometer";


    /**
     * assuming x comes in ranges of -10 to 10 (EVA dumb default), this represents -10
     */
    private double minimum = 0;

    /**
     * assuming x comes in ranges of -10 to 10 (EVA dumb default), this represents 10
     */
    private double maximum = 5;


    /**
     * when strict, it never goes beyond min and max, even if the EVA optimizer wants it to
     */
    private boolean strict = false;

    private boolean alwaysPositive = false;
    /**
     * when this is set to true, it means the argument could never be a DoubleParameter. This usually doesn't matter
     * but unfortunately it seems that YAML struggles with map<String,Number> and turn them into string,string maps
     */
    private boolean rawNumber = false;

    public MultipleOptimizationParameter() {
    }

    public MultipleOptimizationParameter(List<String> addressesToModify, double minimum, double maximum) {
        this.addressesToModify = addressesToModify;
        this.minimum = minimum;
        this.maximum = maximum;
    }


    @Override
    public int size() {
        return 1;
    }

    @Override
    public String parametrize(Scenario scenario, double[] inputs) {
        double realValue = minimum + ((maximum - minimum) / (10 - (-10))) * (inputs[0] - (-10));


        if (isStrict()) {
            if (realValue < minimum)
                realValue = minimum;
            if (realValue > maximum)
                realValue = maximum;
        }


        if (realValue < 0 & alwaysPositive)
            realValue = 0;


        for (String addressToModify : addressesToModify) {
            if (!rawNumber)
                quickParametrize(scenario, realValue, addressToModify);
            else
                quickParametrizeRawNumber(scenario, realValue, addressToModify);

        }

        return String.valueOf(realValue);

    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    @Override
    public String getName() {
        return addressesToModify.get(0);
    }

    public List<String> getAddressesToModify() {
        return addressesToModify;
    }

    public void setAddressesToModify(List<String> addressesToModify) {
        this.addressesToModify = addressesToModify;
    }

    public double getMinimum() {
        return minimum;
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public double getMaximum() {
        return maximum;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    public boolean isAlwaysPositive() {
        return alwaysPositive;
    }

    public void setAlwaysPositive(boolean alwaysPositive) {
        this.alwaysPositive = alwaysPositive;
    }

    public boolean isRawNumber() {
        return rawNumber;
    }

    public void setRawNumber(boolean rawNumber) {
        this.rawNumber = rawNumber;
    }
}
