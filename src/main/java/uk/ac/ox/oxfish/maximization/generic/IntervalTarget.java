package uk.ac.ox.oxfish.maximization.generic;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

/**
 * interval target; set up as JavaBean to be easily changeable from YAML
 */
public class IntervalTarget {

    private String nameOfYearlyColumn = "NoData";

    private double minimum = 0;

    private double maximum = 0;

    private int lag = 0;

    public IntervalTarget() {
    }


    public IntervalTarget(String nameOfYearlyColumn, double minimum, double maximum, int lag) {
        this.nameOfYearlyColumn = nameOfYearlyColumn;
        this.minimum = minimum;
        this.maximum = maximum;
        this.lag = lag;
    }

    public boolean[] test(FishState state)
    {

        final DataColumn column = state.getYearlyDataSet().getColumn(nameOfYearlyColumn);
        boolean[] toReturn = new boolean[column.size()];

        for (int i = 0; i < lag; i++) {
            toReturn[i] = false;
        }


        //lag is 2
        //then at time 0 it should be false
        //at time 1 it should be false
        //at time 2 it should check the value of the column at time 0
        //at time 3 ...



        for (int i = lag; i < column.size(); i++)
            toReturn[i] = (Double.isFinite(column.get(i-lag)) &&
                    column.get(i-lag)>=minimum && column.get(i-lag)<=maximum);


        return toReturn;

    }

    public String getNameOfYearlyColumn() {
        return nameOfYearlyColumn;
    }

    public void setNameOfYearlyColumn(String nameOfYearlyColumn) {
        this.nameOfYearlyColumn = nameOfYearlyColumn;
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

    public int getLag() {
        return lag;
    }

    public void setLag(int lag) {
        this.lag = lag;
    }
}
