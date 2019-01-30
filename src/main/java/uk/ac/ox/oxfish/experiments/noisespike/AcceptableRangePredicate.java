/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.experiments.noisespike;

import uk.ac.ox.oxfish.model.FishState;

import java.util.function.Predicate;

public class AcceptableRangePredicate {



    private final double minimum;

    private final double maximum;

    private final String columnName;


    public AcceptableRangePredicate(double minimum, double maximum, String columnName) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.columnName = columnName;
    }

    public boolean test(FishState state, int yearToTest)
    {

        Double measure = state.getYearlyDataSet().getColumn(columnName).get(yearToTest);
        System.out.println(measure);

        return measure >= minimum && measure <= maximum;

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
     * Getter for property 'maximum'.
     *
     * @return Value for property 'maximum'.
     */
    public double getMaximum() {
        return maximum;
    }

    /**
     * Getter for property 'columnName'.
     *
     * @return Value for property 'columnName'.
     */
    public String getColumnName() {
        return columnName;
    }
}
