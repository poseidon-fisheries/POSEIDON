package uk.ac.ox.oxfish.model.data.jsonexport;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vividsolutions.jts.geom.Coordinate;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.OutputPlugin;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static uk.ac.ox.oxfish.model.data.jsonexport.JsonExportUtils.seaTileHeight;
import static uk.ac.ox.oxfish.model.data.jsonexport.JsonExportUtils.seaTileWidth;

public class JsonIndonesiaMap implements OutputPlugin, Steppable, AdditionalStartable {
    // TODO: remove `setPrettyPrinting()` once we've reasonably debugged the thing
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String fileName;
    private final Map<String, String> prettyNames = ImmutableMap.of(
        "population0", "4-9 GT",
        "population1", "15-30 GT",
        "population2", ">30 GT",
        "population3", "10-14 GT"
    );
    private Stoppable stoppable;
    private JsonOutput jsonOutput;
    final private String modelDescription;

    JsonIndonesiaMap(String fileName, String modelDescription) {
        this.fileName = fileName;
        this.modelDescription = modelDescription;
    }

    @Override
    public void reactToEndOfSimulation(FishState state) {

    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String composeFileContents() {
        return gson.toJson(jsonOutput);
    }

    @Override
    public void step(SimState simState) {
        final MersenneTwisterFast random = (MersenneTwisterFast) simState.random.clone();
        final FishState model = (FishState) simState;
        final List<JsonVesselPosition> jsonVesselPositions = new ArrayList<>();
        JsonTimestep jsonTimestep = new JsonTimestep(model.getDay(), jsonVesselPositions);

        final NauticalMap map = ((FishState) simState).getMap();
        double width = seaTileWidth(map);
        double height = seaTileHeight(map);

        for (Fisher fisher : model.getFishers()) {
            final Coordinate coordinates = new Coordinate(model.getMap().getCoordinates(fisher.getLocation()));
            if (!fisher.isAtPort()) {
                coordinates.x = coordinates.x + (random.nextDouble() - 0.5) * width;
                coordinates.y = coordinates.y + (random.nextDouble() - 0.5) * height;
            }
            jsonVesselPositions.add(new JsonVesselPosition(fisher.getID(), coordinates.x, coordinates.y));
        }
        jsonOutput.timesteps.add(jsonTimestep);
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
        final ArrayList<JsonPort> ports = new ArrayList<>();
        for (Port port : model.getPorts()) {
            final Coordinate coordinates = model.getMap().getCoordinates(port.getLocation());
            ports.add(new JsonPort(coordinates.x, coordinates.y));
        }
        final ArrayList<JsonVessel> vessels = new ArrayList<>();
        for (Fisher fisher : model.getFishers()) {
            final String typeString = prettyNames.get(fisher.getTags().stream()
                .filter(t -> !t.isEmpty())
                .filter(t -> t.contains("population")) // TODO: make that configurable somehow...
                .collect(joining(", ")));
            vessels.add(new JsonVessel(fisher.getID(), typeString));
        }
        jsonOutput = new JsonOutput(
                modelDescription,
            vessels,
            ports,
            new ArrayList<>(), // time steps
            Instant.parse("2018-01-01T00:00:00.00Z").getEpochSecond()
        );
        stoppable = model.scheduleEveryDay(this, StepOrder.AFTER_DATA);
        model.getOutputPlugins().add(this);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        if (stoppable != null) {
            stoppable.stop();
        }
    }
}
