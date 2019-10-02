package uk.ac.ox.oxfish.model.data.jsonexport;

import java.util.List;

public class JsonSimulationSet {

    private final String name;
    private final String mapPath;
    private final List<String> chartPaths;
    private final List<String> regionPaths;

    public JsonSimulationSet(String name, String mapPath, List<String> chartPaths, List<String> regionPaths) {
        this.name = name;
        this.mapPath = mapPath;
        this.chartPaths = chartPaths;
        this.regionPaths = regionPaths;
    }
}
