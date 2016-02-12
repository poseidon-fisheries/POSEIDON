package uk.ac.ox.oxfish.utility.parameters;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import ec.util.MersenneTwisterFast;

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
    public Double apply(MersenneTwisterFast mersenneTwisterFast){
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
}
