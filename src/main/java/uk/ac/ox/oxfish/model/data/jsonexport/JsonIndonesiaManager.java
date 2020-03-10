package uk.ac.ox.oxfish.model.data.jsonexport;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.OutputPlugin;
import uk.ac.ox.oxfish.model.data.jsonexport.heatmap.JsonHeatmapManager;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class JsonIndonesiaManager implements AdditionalStartable, OutputPlugin {

    private JsonIndonesiaCharts jsonIndonesiaCharts;
    private JsonIndonesiaMap jsonIndonesiaMap;
    private JsonRegionsManager jsonRegionsManager;
    private List<JsonHeatmapManager> jsonHeatmapManagers;
    private int numYearsToSkip;

    /**
     * pretty name for dashboard
     */
    private final String simulationTitle;

    /**
     * prefix for all the file names
     */
    final private String filePrefix;

    private final String modelDescription;

    public JsonIndonesiaManager(
            String filePrefix, int numYearsToSkip, String simulationTitle, String modelDescription) {
        this.filePrefix = filePrefix;
        this.numYearsToSkip = numYearsToSkip;
        this.simulationTitle = simulationTitle;
        this.modelDescription = modelDescription;
        this.jsonHeatmapManagers = new LinkedList<>();
    }


    @Override
    public void start(FishState model) {
        model.scheduleOnceAtTheBeginningOfYear((Steppable) simState -> {
            jsonIndonesiaMap = new JsonIndonesiaMap(filePrefix + "_map.json", modelDescription);
            jsonIndonesiaMap.start(model);
            jsonIndonesiaCharts = new JsonIndonesiaCharts(filePrefix, numYearsToSkip-1);
            jsonIndonesiaCharts.start(model);
            jsonRegionsManager = new JsonRegionsManager(filePrefix + "_regions.json");
            jsonRegionsManager.start(model);
            for (Species species : model.getSpecies()) {
                JsonHeatmapManager heatmap = new JsonHeatmapManager("Biomass " + species.getName(),
                                                              filePrefix + "_biomass_" + species.getIndex() +
                                                                      "_heatmap.json",
                                                              new Function<SeaTile, Double>() {
                                                                  @Override
                                                                  public Double apply(SeaTile seaTile) {
                                                                      return FishStateUtilities.round(seaTile.getBiomass(species));
                                                                  }
                                                              });
                heatmap.start(model);
                jsonHeatmapManagers.add(
                        heatmap
                );

            }
        }, StepOrder.DAWN, numYearsToSkip);
        model.getOutputPlugins().add(this);
    }

    @Override
    public void turnOff() {
        jsonIndonesiaMap.turnOff();
        jsonIndonesiaCharts.turnOff();
        for (JsonHeatmapManager heatmapManager : jsonHeatmapManagers) {
            heatmapManager.turnOff();
        }
    }

    @Override
    public void reactToEndOfSimulation(FishState state) { }

    @Override
    public String getFileName() {
        return filePrefix + "_index.json";
    }

    @Override
    public String composeFileContents() {
        final List<String> chartPaths =
            jsonIndonesiaCharts.getChartManagers().stream().map(JsonChartManager::getFileName).collect(toList());
        final ImmutableList<String> regionPaths = ImmutableList.of(jsonRegionsManager.getFileName());



        final JsonSimulationSet jsonSimulationSet =
            new JsonSimulationSet(simulationTitle, jsonIndonesiaMap.getFileName(), chartPaths, regionPaths,
                                  jsonHeatmapManagers.stream().map(JsonHeatmapManager::getFileName).collect(Collectors.toList()));
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonSimulationSet);
    }
}
