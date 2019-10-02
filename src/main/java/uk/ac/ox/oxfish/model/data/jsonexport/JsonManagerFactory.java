package uk.ac.ox.oxfish.model.data.jsonexport;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class JsonManagerFactory implements AlgorithmFactory<JsonIndonesiaManager> {

    private String simulationName = "test";
    private int numYearsToSkip = 0;

    public int getNumYearsToSkip() {
        return numYearsToSkip;
    }

    public void setNumYearsToSkip(int numYearsToSkip) {
        this.numYearsToSkip = numYearsToSkip;
    }

    public String getSimulationName() { return simulationName; }

    public void setSimulationName(String simulationName) { this.simulationName = simulationName; }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public JsonIndonesiaManager apply(FishState fishState) {
        return new JsonIndonesiaManager(simulationName, numYearsToSkip);
    }
}
