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

import uk.ac.ox.oxfish.model.FishState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.lang.Math.abs;

/**
 * computes error by the last number in the column against a fixed number
 */
public class FixedDataLastStepTarget implements FixedDataTarget {


    public static boolean VERBOSE = false;
    private double fixedTarget = 100;
    private double exponent = 1;
    private String columnName = "Average Cash-Flow";


    public FixedDataLastStepTarget() {
    }

    public FixedDataLastStepTarget(double fixedTarget, String columnName) {
        this.fixedTarget = fixedTarget;
        this.columnName = columnName;
    }

    /**
     * extracts fixed target as last element of a csv you provided
     *
     * @param file       single column CSV. The fixed target will the last line
     * @param columnName name of the column IN THE MODEL that you are trying to match
     * @return
     */
    public static FixedDataLastStepTarget lastStepTarget(
        Path file, String columnName
    ) {
        try {
            List<String> strings = Files.readAllLines(file);
            return new FixedDataLastStepTarget(
                Double.parseDouble(
                    strings.get(strings.size() - 1)),
                columnName
            );
        } catch (IOException e) {

            throw new RuntimeException("can't read " + file + " because of " + e);
        }


    }

    /**
     * computes distance from target (0 best, the higher the number the further away from optimum we are)
     *
     * @param model model after it has been run
     * @return distance from target (0 best, the higher the number the further away from optimum we are)
     */
    @Override
    public double computeError(FishState model) {

        final double value = getValue(model);
        final double error = Math.pow(abs(value - fixedTarget), exponent);

        if (VERBOSE) {
            System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
            System.out.println("column: " + columnName);
            System.out.println("output: " + value);
            System.out.println("target: " + fixedTarget);
            System.out.println("error : " + error);
        }

        return error;
    }

    @Override
    public double getValue(final FishState fishState) {
        return fishState.getYearlyDataSet().getColumn(columnName).getLatest();
    }

    /**
     * Getter for property 'fixedTarget'.
     *
     * @return Value for property 'fixedTarget'.
     */
    @Override
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
     * Getter for property 'columnName'.
     *
     * @return Value for property 'columnName'.
     */
    @Override
    public String getColumnName() {
        return columnName;
    }

    /**
     * Setter for property 'columnName'.
     *
     * @param columnName Value to set for property 'columnName'.
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }


    public double getExponent() {
        return exponent;
    }

    public void setExponent(double exponent) {
        this.exponent = exponent;
    }
}
