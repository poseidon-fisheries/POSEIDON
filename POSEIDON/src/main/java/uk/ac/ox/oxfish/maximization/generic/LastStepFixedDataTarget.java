/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2025, University of Oxford.
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

import static com.google.common.base.Preconditions.checkNotNull;

public class LastStepFixedDataTarget implements FixedDataTarget {

    private static final long serialVersionUID = -1736401434755470061L;
    private final ErrorMeasure errorMeasure;
    private double fixedTarget;
    private double weight;
    private boolean verbose;
    private String columnName = "";

    public LastStepFixedDataTarget(final ErrorMeasure errorMeasure) {
        this.errorMeasure = errorMeasure;
    }

    public LastStepFixedDataTarget(
        final ErrorMeasure errorMeasure,
        final String columnName,
        final double fixedTarget
    ) {
        this(errorMeasure, columnName, fixedTarget, 1, false);
    }

    public LastStepFixedDataTarget(
        final ErrorMeasure errorMeasure,
        final String columnName,
        final double fixedTarget,
        final double weight,
        final boolean verbose
    ) {
        this.errorMeasure = errorMeasure;
        this.fixedTarget = fixedTarget;
        this.weight = weight;
        this.verbose = verbose;
        this.columnName = columnName;
    }

    public ErrorMeasure getErrorMeasure() {
        return errorMeasure;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(final double weight) {
        this.weight = weight;
    }

    @Override
    public double computeError(final FishState model) {
        final double value = getValue(model);
        final double error = errorMeasure.applyAsDouble(fixedTarget, value) * weight;
        if (verbose) printResult(value, error);
        return error;
    }

    @Override
    public double getValue(final FishState fishState) {
        final DataColumn column = fishState.getYearlyDataSet().getColumn(columnName);
        checkNotNull(column, "Column " + columnName + " no found");
        return column.getLatest();
    }

    private void printResult(
        final double value,
        final double error
    ) {
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        System.out.println("column: " + getColumnName());
        System.out.println("output: " + value);
        System.out.println("target: " + getFixedTarget());
        System.out.println("error : " + error);
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    @Override
    public double getFixedTarget() {
        return fixedTarget;
    }

    public void setFixedTarget(final double fixedTarget) {
        this.fixedTarget = fixedTarget;
    }

    public void setColumnName(final String columnName) {
        this.columnName = columnName;
    }

}
