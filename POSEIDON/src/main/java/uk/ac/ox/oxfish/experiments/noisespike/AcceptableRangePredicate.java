/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2019-2025, University of Oxford.
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

package uk.ac.ox.oxfish.experiments.noisespike;

import com.google.common.annotations.VisibleForTesting;
import uk.ac.ox.oxfish.model.FishState;

public class AcceptableRangePredicate {


    private double minimum;

    private double maximum;

    private String columnName;


    /**
     * building with setters is pointless, but it works for YAML constructors
     */
    @Deprecated
    @VisibleForTesting
    public AcceptableRangePredicate() {
        minimum = 0;
        maximum = -1;
        columnName = "No column";

    }

    public AcceptableRangePredicate(double minimum, double maximum, String columnName) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.columnName = columnName;
    }

    public double distance(FishState state, int yearToTest) {


        double between = (maximum + minimum) / 2d;
        if (test(state, yearToTest))
            return 0;

        double measure = measure(state, yearToTest);
        if (measure > maximum)
            return (measure - maximum) / between;
        else {
            assert measure < minimum;
            return (minimum - measure) / between;

        }


    }

    public boolean test(FishState state, int yearToTest) {

        double measure = measure(state, yearToTest);
        System.out.println(measure + " ---" + ((measure >= minimum && measure <= maximum) ? "okay" : "fail!"));

        return measure >= minimum && measure <= maximum;

    }

    public double measure(FishState state, int yearToTest) {
        return state.getYearlyDataSet().getColumn(columnName).get(yearToTest);
    }

    /**
     * Getter for property 'minimum'.
     *
     * @return Value for property 'minimum'.
     */
    public double getMinimum() {
        return minimum;
    }

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

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    /**
     * Getter for property 'columnName'.
     *
     * @return Value for property 'columnName'.
     */
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
}
