package uk.ac.ox.oxfish.gui;

import uk.ac.ox.oxfish.model.BatchRunner;

import java.nio.file.Paths;
import java.util.Arrays;

public class BatchRunnerFactory
{
    private String yamlFile = "./inputs/first_paper/fronts.yaml";
    private int yearsToRun = 20;
    private String outputFolder = "outputs/batch";
    private String policyFile = null;
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
                Paths.get(yamlFile),
                yearsToRun,
                Arrays.asList(columnsToPrint.split(",")),
                outputFolder == null ? null : Paths.get(outputFolder),
                policyFile == null ? null : Paths.get(policyFile),
                randomSeed,
                heatmapGathererStartYear);
    }

    /**
     * Getter for property 'yamlFile'.
     *
     * @return Value for property 'yamlFile'.
     */
    public String getYamlFile() {
        return yamlFile;
    }

    /**
     * Setter for property 'yamlFile'.
     *
     * @param yamlFile Value to set for property 'yamlFile'.
     */
    public void setYamlFile(String yamlFile) {
        this.yamlFile = yamlFile;
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
     * Getter for property 'outputFolder'.
     *
     * @return Value for property 'outputFolder'.
     */
    public String getOutputFolder() {
        return outputFolder;
    }

    /**
     * Setter for property 'outputFolder'.
     *
     * @param outputFolder Value to set for property 'outputFolder'.
     */
    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    /**
     * Getter for property 'policyFile'.
     *
     * @return Value for property 'policyFile'.
     */
    public String getPolicyFile() {
        return policyFile;
    }

    /**
     * Setter for property 'policyFile'.
     *
     * @param policyFile Value to set for property 'policyFile'.
     */
    public void setPolicyFile(String policyFile) {
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