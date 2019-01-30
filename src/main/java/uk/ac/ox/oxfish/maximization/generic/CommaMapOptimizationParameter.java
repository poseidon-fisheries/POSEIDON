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
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * always returns a string concatenating many numbers through a comma in a map format:
 * "0:inputone,1:inputtwo"
 */
public class CommaMapOptimizationParameter implements OptimizationParameter {


    private int size = 5;


    private String addressToModify = "literPerKilometer";


    /**
     * assuming x comes in ranges of -10 to 10 (EVA dumb default), this represents -10
     */
    private double minimum = 0;

    /**
     * assuming x comes in ranges of -10 to 10 (EVA dumb default), this represents 10
     */
    private double maximum = 5;


    public CommaMapOptimizationParameter() {
    }

    public CommaMapOptimizationParameter(int size,
                                         String addressToModify,
                                         double minimum,
                                         double maximum) {
        this.size = size;
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
        return size;
    }

    /**
     * consume the scenario and add the parameters
     *
     * @param scenario the scenario to modify
     * @param inputs   the numerical values of the parameters to set
     */
    @Override
    public double parametrize(Scenario scenario, double[] inputs) {

        Preconditions.checkArgument(inputs.length==size);
        Preconditions.checkArgument(size>0);
        Preconditions.checkArgument(maximum>=minimum);

        double realValue = ((inputs[0]+10)/20)*(maximum-minimum);
        StringBuffer buffer = new StringBuffer().append("0:").append(realValue);


        for(int i=1; i<size; i++)
        {
            buffer.append(",").
                    append(i).append(":").
                    append(((inputs[i]+10)/20)*(maximum-minimum));
        }

        //try as double parameter
        try{
        OptimizationParameter.navigateAndSet(
                scenario,addressToModify,buffer.toString()

        );}
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }


        return realValue;


    }


    /**
     * Getter for property 'size'.
     *
     * @return Value for property 'size'.
     */
    public int getSize() {
        return size;
    }

    /**
     * Setter for property 'size'.
     *
     * @param size Value to set for property 'size'.
     */
    public void setSize(int size) {
        this.size = size;
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
