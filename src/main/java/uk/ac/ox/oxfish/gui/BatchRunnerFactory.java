/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.gui;

import uk.ac.ox.oxfish.model.BatchRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class BatchRunnerFactory
{
    private Path yamlFile = Paths.get(".","inputs","first_paper","fronts.yaml");
    private int yearsToRun = 20;
    private Path outputFolder = Paths.get("outputs","batch");
    private Path policyFile = null;
    private long randomSeed = System.currentTimeMillis();
    private String columnsToPrint = "Total Effort,Average Cash-Flow ";
    private int numberOfRuns = 100;
    private Integer heatmapGathererStartYear = -1;

    /**
     * Gets a result.
     *
     * @return a result
     */
    public BatchRunner build() {
        return new BatchRunner(
                yamlFile,
                yearsToRun,
                Arrays.asList(columnsToPrint.split(",")),
                outputFolder,
                policyFile,
                randomSeed,
                heatmapGathererStartYear);
    }



    /**
     * Getter for property 'yearsToRun'.
     *
     * @return Value for property 'yearsToRun'.
     */
    public int getYearsToRun() {
        return yearsToRun;
    }

    /**
     * Setter for property 'yearsToRun'.
     *
     * @param yearsToRun Value to set for property 'yearsToRun'.
     */
    public void setYearsToRun(int yearsToRun) {
        this.yearsToRun = yearsToRun;
    }


    /**
     * Getter for property 'yamlFile'.
     *
     * @return Value for property 'yamlFile'.
     */
    public Path getYamlFile() {
        return yamlFile;
    }

    /**
     * Setter for property 'yamlFile'.
     *
     * @param yamlFile Value to set for property 'yamlFile'.
     */
    public void setYamlFile(Path yamlFile) {
        this.yamlFile = yamlFile;
    }

    /**
     * Getter for property 'outputFolder'.
     *
     * @return Value for property 'outputFolder'.
     */
    public Path getOutputFolder() {
        return outputFolder;
    }

    /**
     * Setter for property 'outputFolder'.
     *
     * @param outputFolder Value to set for property 'outputFolder'.
     */
    public void setOutputFolder(Path outputFolder) {
        this.outputFolder = outputFolder;
    }

    /**
     * Getter for property 'policyFile'.
     *
     * @return Value for property 'policyFile'.
     */
    public Path getPolicyFile() {
        return policyFile;
    }

    /**
     * Setter for property 'policyFile'.
     *
     * @param policyFile Value to set for property 'policyFile'.
     */
    public void setPolicyFile(Path policyFile) {
        this.policyFile = policyFile;
    }

    /**
     * Getter for property 'randomSeed'.
     *
     * @return Value for property 'randomSeed'.
     */
    public long getRandomSeed() {
        return randomSeed;
    }

    /**
     * Setter for property 'randomSeed'.
     *
     * @param randomSeed Value to set for property 'randomSeed'.
     */
    public void setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
    }

    /**
     * Getter for property 'columnsToPrint'.
     *
     * @return Value for property 'columnsToPrint'.
     */
    public String getColumnsToPrint() {
        return columnsToPrint;
    }

    /**
     * Setter for property 'columnsToPrint'.
     *
     * @param columnsToPrint Value to set for property 'columnsToPrint'.
     */
    public void setColumnsToPrint(String columnsToPrint) {
        this.columnsToPrint = columnsToPrint;
    }

    /**
     * Getter for property 'numberOfRuns'.
     *
     * @return Value for property 'numberOfRuns'.
     */
    public int getNumberOfRuns() {
        return numberOfRuns;
    }

    /**
     * Setter for property 'numberOfRuns'.
     *
     * @param numberOfRuns Value to set for property 'numberOfRuns'.
     */
    public void setNumberOfRuns(int numberOfRuns) {
        this.numberOfRuns = numberOfRuns;
    }


    /**
     * Getter for property 'heatmapGathererStartYear'.
     *
     * @return Value for property 'heatmapGathererStartYear'.
     */
    public Integer getHeatmapGathererStartYear() {
        return heatmapGathererStartYear;
    }

    /**
     * Setter for property 'heatmapGathererStartYear'.
     *
     * @param heatmapGathererStartYear Value to set for property 'heatmapGathererStartYear'.
     */
    public void setHeatmapGathererStartYear(Integer heatmapGathererStartYear) {
        this.heatmapGathererStartYear = heatmapGathererStartYear;
    }
}