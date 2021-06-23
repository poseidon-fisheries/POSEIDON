/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.maximization.generic;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractLastStepFixedDataTarget implements FixedDataTarget {

    private final ForecastErrorMeasure forecastErrorMeasure;

    private double fixedTarget;
    private double weight;
    private boolean verbose;
    private String columnName = "";

    AbstractLastStepFixedDataTarget(final ForecastErrorMeasure forecastErrorMeasure) {
        this(forecastErrorMeasure, 1, false);
    }

    AbstractLastStepFixedDataTarget(
        final ForecastErrorMeasure forecastErrorMeasure,
        final double weight,
        final boolean verbose
    ) {
        this.forecastErrorMeasure = forecastErrorMeasure;
        this.weight = weight;
        this.verbose = verbose;
    }

    public boolean isVerbose() { return verbose; }

    public void setVerbose(final boolean verbose) { this.verbose = verbose; }

    public double getWeight() { return weight; }

    public void setWeight(final double weight) { this.weight = weight; }

    @Override
    public double computeError(FishState model) {
        final double value = getValue(model);
        final double error = forecastErrorMeasure.applyAsDouble(fixedTarget, value) * weight;
        if (verbose) printResult(value, error);
        return error;
    }

    private void printResult(final double value, final double error) {
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        System.out.println("column: " + getColumnName());
        System.out.println("output: " + value);
        System.out.println("target: " + getFixedTarget());
        System.out.println("error : " + error);
    }

    @Override public double getFixedTarget() { return fixedTarget; }

    @Override public double getValue(final FishState fishState) {
        DataColumn column = fishState.getYearlyDataSet().getColumn(columnName);
        checkNotNull(column, "Column " + columnName + " no found");
        return column.getLatest();
    }

    @Override public String getColumnName() { return columnName; }

    public void setColumnName(String columnName) { this.columnName = columnName; }

    public void setFixedTarget(double fixedTarget) {
        this.fixedTarget = fixedTarget;
    }



}
