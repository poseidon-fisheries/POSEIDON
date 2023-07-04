/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
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

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

/**
 * like last step target, but with different exponent above and below
 */
public class AsymmetricFixedDataLastStepTarget implements DataTarget {


    private static final long serialVersionUID = 7106016076182841188L;
    public static boolean VERBOSE = true;
    private double fixedTarget = 100;
    private double exponentAbove = 1;
    private double exponentBelow = 1;
    private String columnName = "Average Cash-Flow";


    public AsymmetricFixedDataLastStepTarget(
        final double fixedTarget, final double exponentAbove, final double exponentBelow, final String columnName
    ) {
        this.fixedTarget = fixedTarget;
        this.exponentAbove = exponentAbove;
        this.exponentBelow = exponentBelow;
        this.columnName = columnName;
    }


    public AsymmetricFixedDataLastStepTarget() {
    }

    /**
     * computes distance from target (0 best, the higher the number the further away from optimum we are)
     *
     * @param model model after it has been run
     * @return distance from target (0 best, the higher the number the further away from optimum we are)
     */
    @Override
    public double computeError(final FishState model) {

        final DataColumn simulationOutput = model.getYearlyDataSet().getColumn(columnName);

        final double error;
        if (simulationOutput.getLatest() > fixedTarget)
            error = Math.pow(Math.abs(simulationOutput.getLatest() - fixedTarget), exponentAbove);
        else
            error = Math.pow(Math.abs(simulationOutput.getLatest() - fixedTarget), exponentBelow);


        if (VERBOSE) {
            System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
            System.out.println("column: " + columnName);
            System.out.println("output: " + simulationOutput.getLatest());
            System.out.println("target : " + fixedTarget);
            System.out.println("error : " + error);
        }

        return error;

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
    public void setFixedTarget(final double fixedTarget) {
        this.fixedTarget = fixedTarget;
    }

    /**
     * Getter for property 'columnName'.
     *
     * @return Value for property 'columnName'.
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Setter for property 'columnName'.
     *
     * @param columnName Value to set for property 'columnName'.
     */
    public void setColumnName(final String columnName) {
        this.columnName = columnName;
    }


    /**
     * Getter for property 'exponentAbove'.
     *
     * @return Value for property 'exponentAbove'.
     */
    public double getExponentAbove() {
        return exponentAbove;
    }

    /**
     * Setter for property 'exponentAbove'.
     *
     * @param exponentAbove Value to set for property 'exponentAbove'.
     */
    public void setExponentAbove(final double exponentAbove) {
        this.exponentAbove = exponentAbove;
    }

    /**
     * Getter for property 'exponentBelow'.
     *
     * @return Value for property 'exponentBelow'.
     */
    public double getExponentBelow() {
        return exponentBelow;
    }

    /**
     * Setter for property 'exponentBelow'.
     *
     * @param exponentBelow Value to set for property 'exponentBelow'.
     */
    public void setExponentBelow(final double exponentBelow) {
        this.exponentBelow = exponentBelow;
    }


}
