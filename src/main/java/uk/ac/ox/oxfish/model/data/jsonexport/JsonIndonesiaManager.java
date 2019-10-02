package uk.ac.ox.oxfish.model.data.jsonexport;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.OutputPlugin;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class JsonIndonesiaManager implements AdditionalStartable, OutputPlugin {

    private JsonIndonesiaCharts jsonIndonesiaCharts;
    private JsonIndonesiaMap jsonIndonesiaMap;
    private JsonRegionsManager jsonRegionsManager;
    private int numYearsToSkip;
    private String simulationName;

    public JsonIndonesiaManager(String simulationName, int numYearsToSkip) {
        this.simulationName = simulationName;
        this.numYearsToSkip = numYearsToSkip;
    }

    @Override
    public void start(FishState model) {
        model.scheduleOnceAtTheBeginningOfYear((Steppable) simState -> {
            jsonIndonesiaMap = new JsonIndonesiaMap(simulationName + "_map.json");
            jsonIndonesiaMap.start(model);
            jsonIndonesiaCharts = new JsonIndonesiaCharts(simulationName, numYearsToSkip);
            jsonIndonesiaCharts.start(model);
            jsonRegionsManager = new JsonRegionsManager(simulationName + "_regions.json");
            jsonRegionsManager.start(model);
        }, StepOrder.DAWN, numYearsToSkip);
        model.getOutputPlugins().add(this);
    }

    @Override
    public void turnOff() {
        jsonIndonesiaMap.turnOff();
        jsonIndonesiaCharts.turnOff();
    }

    @Override
    public void reactToEndOfSimulation(FishState state) { }

    @Override
    public String getFileName() {
        return simulationName + "_index.json";
    }

    @Override
    public String composeFileContents() {
        final List<String> chartPaths =
            jsonIndonesiaCharts.getChartManagers().stream().map(JsonChartManager::getFileName).collect(toList());
        final ImmutableList<String> regionPaths = ImmutableList.of(jsonRegionsManager.getFileName());
        final JsonSimulationSet jsonSimulationSet =
            new JsonSimulationSet(simulationName, jsonIndonesiaMap.getFileName(), chartPaths, regionPaths);
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonSimulationSet);
    }
}
