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

import com.google.common.base.Preconditions;
import org.apache.commons.beanutils.PropertyUtils;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import javax.swing.*;

/**
 *
 */
public class SimpleOptimizationParameter implements OptimizationParameter {


    private String addressToModify = "literPerKilometer";


    /**
     * assuming x comes in ranges of -10 to 10 (EVA dumb default), this represents -10
     */
    private double minimum = 0;

    /**
     * assuming x comes in ranges of -10 to 10 (EVA dumb default), this represents 10
     */
    private double maximum = 5;


    public SimpleOptimizationParameter() {
    }

    public SimpleOptimizationParameter(String addressToModify, double minimum, double maximum) {
        this.addressToModify = addressToModify;
        this.minimum = minimum;
        this.maximum = maximum;
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

        Preconditions.checkArgument(maximum>=minimum, "invalid bounds " + addressToModify);
        Preconditions.checkArgument(inputs.length==1);

        double realValue =minimum+((maximum-minimum)/(10-(-10)))*(inputs[0]- (-10));

        //i am just going to do this the hackish way. The input could be a DoubleParameter or a straight up number. I will try the first, catch the exception
        // and try the second

        quickParametrize(scenario, realValue, addressToModify);

        return realValue;

    }

    public double parametrizeRealValue(Scenario scenario, double realValue)
    {
        quickParametrize(scenario, realValue, addressToModify);
        return realValue;

    }

    static void quickParametrize(Scenario scenario, double realValue, String addressToModify) {
        try{
            //try as double parameter
            OptimizationParameter.navigateAndSet(
                    scenario,addressToModify,new FixedDoubleParameter(realValue)

            );
        }catch (Exception e){
            //try as raw number
            try{
            OptimizationParameter.navigateAndSet(
                    scenario,addressToModify,realValue

            );}
            catch (Exception d)
            {
                throw new RuntimeException(d);
            }
        }
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
    public void setAddressToModify(String addressToModify) {
        this.addressToModify = addressToModify;
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
    public void setMinimum(double minimum) {
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
    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }
}
