package uk.ac.ox.oxfish.model.data.jsonexport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vividsolutions.jts.geom.Coordinate;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.OutputPlugin;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class JsonManager implements OutputPlugin, Steppable, AdditionalStartable {
    // TODO: remove `setPrettyPrinting()` once we've reasonably debugged the thing
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Stoppable stoppable;
    private JsonOutput jsonOutput;

    @Override
    public void reactToEndOfSimulation(FishState state) {

    }

    @Override
    public String getFileName() {
        // TODO: get this from somewhere else
        return "test_map.json";
    }

    @Override
    public String composeFileContents() {
        return gson.toJson(jsonOutput);
    }

    @Override
    public void step(SimState simState) {
        final FishState model = (FishState) simState;
        final List<JsonVesselPosition> jsonVesselPositions = new ArrayList<>();
        JsonTimestep jsonTimestep = new JsonTimestep(model.getDay(), jsonVesselPositions);
        for (Fisher fisher : model.getFishers()) {
            final Coordinate coordinates = model.getMap().getCoordinates(fisher.getLocation());
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
            final String typeString = fisher.getTags().stream()
                .filter(t -> !t.isEmpty())
                .filter(t -> t.contains("population")) // TODO: make that configurable somehow...
                .collect(joining(", "));
            vessels.add(new JsonVessel(fisher.getID(), typeString));
        }
        jsonOutput = new JsonOutput(
            "lorem ipsum",
            vessels,
            ports,
            new ArrayList<>(), // timesteps
            Instant.now().getEpochSecond()
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
