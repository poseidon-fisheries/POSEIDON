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

    public boolean[] test(FishState state) {

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
            toReturn[i] = (Double.isFinite(column.get(i - lag)) &&
                column.get(i - lag) >= minimum && column.get(i - lag) <= maximum);


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
