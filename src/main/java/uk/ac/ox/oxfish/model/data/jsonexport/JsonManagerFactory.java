package uk.ac.ox.oxfish.model.data.jsonexport;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class JsonManagerFactory implements AlgorithmFactory<JsonIndonesiaManager> {

    private String filePrefix = "test";

    private String dashboardName = "Test";

    private int numYearsToSkip = 0;

    public int getNumYearsToSkip() {
        return numYearsToSkip;
    }

    public void setNumYearsToSkip(int numYearsToSkip) {
        this.numYearsToSkip = numYearsToSkip;
    }

    public String getFilePrefix() { return filePrefix; }

    public void setFilePrefix(String filePrefix) { this.filePrefix = filePrefix; }

    /**
     * Getter for property 'dashboardName'.
     *
     * @return Value for property 'dashboardName'.
     */
    public String getDashboardName() {
        return dashboardName;
    }

    /**
     * Setter for property 'dashboardName'.
     *
     * @param dashboardName Value to set for property 'dashboardName'.
     */
    public void setDashboardName(String dashboardName) {
        this.dashboardName = dashboardName;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public JsonIndonesiaManager apply(FishState fishState) {
        return new JsonIndonesiaManager(filePrefix, numYearsToSkip, dashboardName);


    }
}
