/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.utility.parameters;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import ec.util.MersenneTwisterFast;

import java.util.Arrays;

/**
 * Selects uniformly from a list of possible values. Keeps a "string" representation to be useful with the GUI
 * Created by carrknight on 2/11/16.
 */
public class SelectDoubleParameter implements DoubleParameter {

    private double[] possibleValues;

    private String valueString;

    public SelectDoubleParameter(double[] possibleValues) {
        Preconditions.checkArgument(possibleValues.length > 0, "The select list is empty!");
        this.possibleValues = possibleValues;
        arrayToString();
    }

    public SelectDoubleParameter(String valueList) {

        //splits by whitespace
        this(stringToArray(valueList));


    }

    public static double[] stringToArray(String valueList) {
        return Splitter.onPattern("\\s+").splitToList(valueList.trim()).stream().
                mapToDouble((s) -> {
                    if(s.matches(".*\\d+.*"))
                        return Double.parseDouble(s);
                    else
                        return Double.NaN;
                }).toArray();
    }


    /**
     * Applies this function to the given argument.
     *
     * @param mersenneTwisterFast the function argument
     * @return the function result
     */
    @Override
    public double applyAsDouble(MersenneTwisterFast mersenneTwisterFast){
        Preconditions.checkArgument(possibleValues.length > 0);

        double possibleValue = possibleValues[mersenneTwisterFast.nextInt(possibleValues.length)];
        if(Double.isNaN(possibleValue))
            throw new IllegalStateException("Select variable contains a NaN!");
        return possibleValue;
    }

    public double[] getPossibleValues() {
        return possibleValues;
    }


    private void arrayToString(){
        StringBuilder builder = new StringBuilder();
        for(double value : possibleValues)
            builder.append(value).append(" ");
        valueString = builder.toString();

    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
        if(valueString.matches(".*\\d+.*")) //don't transform until there is at least a number (ugly hack to keep gui happy)
            this.possibleValues =   stringToArray(valueString);
    }

    @Override
    public DoubleParameter makeCopy() {
        return new SelectDoubleParameter(Arrays.copyOf(possibleValues,possibleValues.length));
    }
}
