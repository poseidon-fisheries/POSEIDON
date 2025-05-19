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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 */
public class SimpleOptimizationParameter implements OptimizationParameter, Serializable {

    private static final long serialVersionUID = 1148689356037897712L;
    private String addressToModify = "literPerKilometer";

    /**
     * assuming x comes in ranges of -10 to 10 (EVA dumb default), this represents -10
     */
    private double minimum = 0;

    /**
     * assuming x comes in ranges of -10 to 10 (EVA dumb default), this represents 10
     */
    private double maximum = 5;

    private boolean alwaysPositive = false;

    /**
     * when this is set to true, it means the argument could never be a DoubleParameter. This
     * usually doesn't matter but unfortunately it seems that YAML struggles with map<String,Number>
     * and turn them into string,string maps
     */
    private boolean isRawNumber = false;

    public SimpleOptimizationParameter() {
    }

    public SimpleOptimizationParameter(
        final String addressToModify,
        final double minimum,
        final double maximum
    ) {
        this.addressToModify = addressToModify;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public SimpleOptimizationParameter(
        final String addressToModify,
        final double minimum,
        final double maximum,
        final boolean alwaysPositive,
        final boolean isRawNumber
    ) {
        this.addressToModify = addressToModify;
        this.minimum = minimum;
        this.maximum = maximum;
        this.alwaysPositive = alwaysPositive;
        this.isRawNumber = isRawNumber;
    }

    public static void quickParametrize(
        final Scenario scenario,
        final double realValue,
        final String addressToModify
    ) {
        try {
            // try as double parameter
            OptimizationParameter.navigateAndSet(
                scenario, addressToModify, new FixedDoubleParameter(realValue)

            );
        } catch (final Exception e) {
            // try as raw number
            try {
                OptimizationParameter.navigateAndSet(
                    scenario, addressToModify, realValue

                );
            } catch (final Exception d) {
                throw new RuntimeException(d);
            }
        }
    }

    public static void quickParametrizeRawNumber(
        final Scenario scenario,
        final double realValue,
        final String addressToModify
    ) {
        // try as raw number
        try {
            OptimizationParameter.navigateAndSet(
                scenario, addressToModify, realValue

            );
        } catch (final Exception d) {
            throw new RuntimeException(d);
        }
    }

    public static double computeNumericValueFromEVABounds(
        final double input,
        final double minimum,
        final double maximum,
        final boolean forcePositive
    ) {
        double realValue = minimum + ((maximum - minimum) / (10 - (-10))) * (input - (-10));
        if (realValue < 0 & forcePositive)
            realValue = 0;
        return realValue;
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
     * @return
     */
    @Override
    public String parametrize(
        final Scenario scenario,
        final double[] inputs
    ) {

        Preconditions.checkArgument(maximum >= minimum, "invalid bounds " + addressToModify);
        Preconditions.checkArgument(inputs.length == 1);

        final double realValue = computeNumericValue(inputs[0]);

        if (!isRawNumber)
            quickParametrize(scenario, realValue, addressToModify);
        else
            quickParametrizeRawNumber(scenario, realValue, addressToModify);

        return String.valueOf(realValue);

    }

    public double computeNumericValue(final double input) {
        return computeNumericValueFromEVABounds(input, minimum, maximum, alwaysPositive);
    }

    public double computeMappedValue(final double realValue) {
        return -10 + ((realValue - minimum) / (maximum - minimum)) * 20;
    }

    public double parametrizeRealValue(
        final Scenario scenario,
        final double realValue
    ) {
        quickParametrize(scenario, realValue, addressToModify);
        return realValue;

    }

    /**
     * Getter for property 'minimum'.
     *
     * @return Value for property 'minimum'.
     */
    public double getMinimum() {
        return minimum;
    }

    /**
     * Setter for property 'minimum'.
     *
     * @param minimum Value to set for property 'minimum'.
     */
    public void setMinimum(final double minimum) {
        this.minimum = minimum;
    }

    /**
     * Getter for property 'maximum'.
     *
     * @return Value for property 'maximum'.
     */
    public double getMaximum() {
        return maximum;
    }

    /**
     * Setter for property 'maximum'.
     *
     * @param maximum Value to set for property 'maximum'.
     */
    public void setMaximum(final double maximum) {
        this.maximum = maximum;
    }

    public boolean isAlwaysPositive() {
        return alwaysPositive;
    }

    public void setAlwaysPositive(final boolean alwaysPositive) {
        this.alwaysPositive = alwaysPositive;
    }

    @Override
    public String getName() {
        return getAddressToModify();
    }

    /**
     * Getter for property 'addressToModify'.
     *
     * @return Value for property 'addressToModify'.
     */
    public String getAddressToModify() {
        return addressToModify;
    }

    /**
     * Setter for property 'addressToModify'.
     *
     * @param addressToModify Value to set for property 'addressToModify'.
     */
    public void setAddressToModify(final String addressToModify) {
        this.addressToModify = addressToModify;
    }

    public boolean isRawNumber() {
        return isRawNumber;
    }

    public void setRawNumber(final boolean rawNumber) {
        isRawNumber = rawNumber;
    }

    public double getValue(final Scenario scenario) {
        return getGetter(scenario).get();
    }

    public Supplier<Double> getGetter(final Scenario scenario) {
        final ParameterAddress parameterAddress = new ParameterAddress(addressToModify);
        return () -> ((FixedDoubleParameter) parameterAddress.getValue(scenario)).getValue();
    }

    public Consumer<Double> getSetter(final Scenario scenario) {
        final ParameterAddress parameterAddress = new ParameterAddress(addressToModify);
        return value -> parameterAddress.setValue(scenario, new FixedDoubleParameter(value));
    }

    @Override
    public String toString() {
        return "SimpleOptimizationParameter{" +
            "addressToModify='" + addressToModify + '\'' +
            ", minimum=" + minimum +
            ", maximum=" + maximum +
            ", alwaysPositive=" + alwaysPositive +
            ", isRawNumber=" + isRawNumber +
            '}';
    }
}
