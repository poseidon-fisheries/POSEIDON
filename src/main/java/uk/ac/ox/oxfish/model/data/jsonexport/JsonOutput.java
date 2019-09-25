package uk.ac.ox.oxfish.model.data.jsonexport;

import java.util.List;

public class JsonOutput {
    String description;
    List<JsonVessel> vessels;
    List<JsonPort> ports;
    List<JsonTimestep> timesteps;
    long start;

    public JsonOutput(
        String description,
        List<JsonVessel> vessels,
        List<JsonPort> ports,
        List<JsonTimestep> timesteps,
        long start
    ) {
        this.description = description;
        this.vessels = vessels;
        this.ports = ports;
        this.timesteps = timesteps;
        this.start = start;
    }
}
