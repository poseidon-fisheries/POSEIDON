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
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;

/**
 * an optimization target where we read a time series from file and compare it with
 * something the model outputted
 */
public class YearlyDataTarget implements DataTarget {


    private double exponent = 1;
    private boolean cumulative;

    private String pathToCsvFile = Paths.
            get("/home/carrknight/code/oxfish/docs/indonesia_hub/runs/712/slice1/calibration/targets/Small_LL021 Lutjanus malabaricus.csv").
            resolve("Small_other.csv").toString();

    private String yearlyDataColumnName = "Other Landings of population0";
    /**
     * does the CSV file have a header? then skip first line
     */
    private boolean hasHeader = true;

    /**
     * useful as a way to compute STD of error (negative or 0 means do not use)
     */

    private  double coefficientOfVariation = .1;


    /**
     *  @param pathToCsvFile
     * @param yearlyDataColumnName
     * @param hasHeader
     * @param coefficientOfVariation can be 0, negative or not a number; will then ignore
     * @param cumulative
     */
    public YearlyDataTarget(
            String pathToCsvFile, String yearlyDataColumnName, boolean hasHeader,
            double coefficientOfVariation,
            double exponent, boolean cumulative) {
        this.pathToCsvFile = pathToCsvFile;
        this.yearlyDataColumnName = yearlyDataColumnName;
        this.hasHeader = hasHeader;
        //can be 0, negative or not a number; will then ignore
        this.coefficientOfVariation = coefficientOfVariation;
        this.exponent = exponent;
        this.cumulative = cumulative;
    }


    public double computeError(FishState model)
    {

        DataColumn simulationOutput = model.getYearlyDataSet().getColumn(yearlyDataColumnName);

        try {
            List<String> lines = Files.readAllLines(Paths.get(pathToCsvFile));
            ArrayList<Double> realData = new ArrayList<>();

            DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
            for (int i = hasHeader ? 1 : 0; i < lines.size(); i++) {
                double realTimeSeriesElement = Double.parseDouble(lines.get(i));
                realData.add(realTimeSeriesElement);
                stats.accept(realTimeSeriesElement);

            }
            double plainDistance = FishStateUtilities.timeSeriesDistance(
                    simulationOutput,
                    realData, exponent,
                    cumulative) / stats.getCount();

            double std = stats.getAverage() * coefficientOfVariation;
            if(std <=0 || !Double.isFinite(std))
                return plainDistance;
            else
            // average distance / std
            {

                return plainDistance / std;
            }

        }
        catch (IOException e){
            throw new RuntimeException(e);
        }

    }

    public YearlyDataTarget() {
    }

    /**
     * Getter for property 'pathToCsvFile'.
     *
     * @return Value for property 'pathToCsvFile'.
     */
    public String getPathToCsvFile() {
        return pathToCsvFile;
    }

    /**
     * Setter for property 'pathToCsvFile'.
     *
     * @param pathToCsvFile Value to set for property 'pathToCsvFile'.
     */
    public void setPathToCsvFile(String pathToCsvFile) {
        this.pathToCsvFile = pathToCsvFile;
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
     * Getter for property 'hasHeader'.
     *
     * @return Value for property 'hasHeader'.
     */
    public boolean isHasHeader() {
        return hasHeader;
    }

    /**
     * Setter for property 'hasHeader'.
     *
     * @param hasHeader Value to set for property 'hasHeader'.
     */
    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    /**
     * Getter for property 'coefficientOfVariation'.
     *
     * @return Value for property 'coefficientOfVariation'.
     */
    public double getCoefficientOfVariation() {
        return coefficientOfVariation;
    }

    /**
     * Setter for property 'coefficientOfVariation'.
     *
     * @param coefficientOfVariation Value to set for property 'coefficientOfVariation'.
     */
    public void setCoefficientOfVariation(double coefficientOfVariation) {
        this.coefficientOfVariation = coefficientOfVariation;
    }

    public double getExponent() {
        return exponent;
    }

    public void setExponent(double exponent) {
        this.exponent = exponent;
    }

    public boolean isCumulative() {
        return cumulative;
    }

    public void setCumulative(boolean cumulative) {
        this.cumulative = cumulative;
    }
}
