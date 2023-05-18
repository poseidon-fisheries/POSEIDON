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

package uk.ac.ox.oxfish.maximization.generic;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

import java.util.DoubleSummaryStatistics;
import java.util.Iterator;

/**
 * takes the average of a time series and compares it to fixed target: error = abs(average-target)/weightInverse
 */
public class AverageDataTarget implements DataTarget {

    private double fixedTarget = 100;

    private String yearlyDataColumnName = "Average Cash-Flow";

    /**
     * error = abs(average-target)/weightInverse
     */
    private double weightInverse = 1;

    /**
     * do not read the first yearsToSkip observations of the simulation output
     */
    private int yearsToSkip = 0;

    /**
     * computes distance from target (0 best, the higher the number the further away from optimum we are)
     *
     * @param model model after it has been run
     * @return distance from target (0 best, the higher the number the further away from optimum we are)
     */
    @Override
    public double computeError(FishState model) {

        DataColumn simulationOutput = model.getYearlyDataSet().getColumn(yearlyDataColumnName);
        DoubleSummaryStatistics statistics = new DoubleSummaryStatistics();
        Iterator<Double> iterator = simulationOutput.iterator();
        for (int skips = 0; skips < yearsToSkip; skips++) {
            iterator.next();
        }
        Preconditions.checkArgument(iterator.hasNext(), "no observations to compute error!");
        iterator.forEachRemaining(statistics::accept);

        return Math.abs(statistics.getAverage() - fixedTarget) / weightInverse;


    }


    /**
     * Getter for property 'fixedTarget'.
     *
     * @return Value for property 'fixedTarget'.
     */
    public double getFixedTarget() {
        return fixedTarget;
    }

    /**
     * Setter for property 'fixedTarget'.
     *
     * @param fixedTarget Value to set for property 'fixedTarget'.
     */
    public void setFixedTarget(double fixedTarget) {
        this.fixedTarget = fixedTarget;
    }

    /**
     * Getter for property 'yearlyDataColumnName'.
     *
     * @return Value for property 'yearlyDataColumnName'.
     */
    public String getYearlyDataColumnName() {
        return yearlyDataColumnName;
    }

    /**
     * Setter for property 'yearlyDataColumnName'.
     *
     * @param yearlyDataColumnName Value to set for property 'yearlyDataColumnName'.
     */
    public void setYearlyDataColumnName(String yearlyDataColumnName) {
        this.yearlyDataColumnName = yearlyDataColumnName;
    }

    /**
     * Getter for property 'weight'.
     *
     * @return Value for property 'weight'.
     */
    public double getWeightInverse() {
        return weightInverse;
    }

    /**
     * Setter for property 'weight'.
     *
     * @param weightInverse Value to set for property 'weight'.
     */
    public void setWeightInverse(double weightInverse) {
        this.weightInverse = weightInverse;
    }

    /**
     * Getter for property 'yearsToSkip'.
     *
     * @return Value for property 'yearsToSkip'.
     */
    public int getYearsToSkip() {
        return yearsToSkip;
    }

    /**
     * Setter for property 'yearsToSkip'.
     *
     * @param yearsToSkip Value to set for property 'yearsToSkip'.
     */
    public void setYearsToSkip(int yearsToSkip) {
        this.yearsToSkip = yearsToSkip;
    }
}
