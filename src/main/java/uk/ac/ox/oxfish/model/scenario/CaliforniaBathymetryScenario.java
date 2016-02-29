package uk.ac.ox.oxfish.model.scenario;

import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.ObjectGrid2D;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.geography.CartesianDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.geography.pathfinding.StraightLinePathfinder;
import uk.ac.ox.oxfish.geography.sampling.SampledMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.SocialNetwork;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.OptionalDouble;

/**
 * Reads the bathymetry file of california and for now not much else.
 * Created by carrknight on 5/7/15.
 */
public class CaliforniaBathymetryScenario implements Scenario {

    private int numberOfSpecies;
    private int gridWidth = 50;


    public CaliforniaBathymetryScenario(int numberOfSpecies) {
        this.numberOfSpecies = numberOfSpecies;
    }

    /**
     * this is the very first method called by the model when it is started. The scenario needs to instantiate all the
     * essential objects for the model to take place
     *
     * @param model the model
     * @return a scenario-result object containing the map, the list of agents and the biology object
     */
    @Override
    public ScenarioEssentials start(FishState model) {

        GlobalBiology biology;
        NauticalMap map;

        try {
            SampledMap sampledMap = new SampledMap(Paths.get("inputs", "california",
                                                             "california.csv"),
                                                   gridWidth,
                                                   Paths.get("inputs","california","biology","spatial",
                                                             "DoverSole.csv"),
                                                   Paths.get("inputs","california","biology","spatial",
                                                             "Longspine.csv"),
                                                   Paths.get("inputs","california","biology","spatial",
                                                             "Sablefish.csv"),
                                                   Paths.get("inputs","california","biology","spatial",
                                                             "Shortspine.csv"),
                                                   Paths.get("inputs","california","biology","spatial",
                                                             "Yelloweye.csv"));

            //we want a grid of numbers but we have a grid where every cell has many observations
            int gridHeight = sampledMap.getGridHeight();
            ObjectGrid2D altitudeGrid = new ObjectGrid2D(gridWidth, gridHeight);
            ObjectGrid2D sampledAltitudeGrid = sampledMap.getAltitudeGrid();
            //so for altitude we just average them out
            for(int x=0;x<gridWidth;x++)
                for(int y=0;y<gridHeight;y++)
                {
                    OptionalDouble average = ((LinkedList<Double>) sampledAltitudeGrid.get(x, y)).stream().mapToDouble(
                            value -> value).filter(
                            aDouble -> aDouble > -9999).average();
                    altitudeGrid.set(x, y,
                                     new SeaTile(x, y, average.orElseGet(() -> 1000d), new TileHabitat(0)));
                }

            LinkedList<Species> species = new LinkedList<>();
            for(String speciesName : sampledMap.getBiologyGrids().keySet())
                species.add(new Species(speciesName));
            biology = new GlobalBiology(species.toArray(new Species[species.size()]));

            GeomGridField unitedMap = new GeomGridField(altitudeGrid);
            unitedMap.setMBR(sampledMap.getMbr());
            //create the map
            map = new NauticalMap(unitedMap, new GeomVectorField(),
                                  new CartesianDistance(1),
                                  new StraightLinePathfinder());
            //now add bio information to it
            for(int x=0;x<gridWidth;x++)
                for(int y=0;y<gridHeight;y++)
                {
                    SeaTile seaTile = map.getSeaTile(x, y);
                    if(seaTile.getAltitude() < 0) {
                        double averages[] = new double[species.size()];
                        int i=0;
                        for(Map.Entry<String,ObjectGrid2D> observations :  sampledMap.getBiologyGrids().entrySet()) {
                            assert species.get(i).getName().equals(observations.getKey());
                            OptionalDouble average = ((LinkedList<Double>) observations.getValue().get(x, y)).stream().mapToDouble(
                                    value -> value).average();
                            averages[i] = average.orElse(0) * 500;
                            i++;
                            if (average.isPresent())
                                seaTile.setBiology(new ConstantLocalBiology(average.getAsDouble() * 500));
                        }
                        seaTile.setBiology(new ConstantHeterogeneousLocalBiology(averages));
                    }
                    else
                        seaTile.setBiology(new EmptyLocalBiology());
                }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Some files were missing!");
        }

        System.out.println("height: " +map.getHeight());
        return new ScenarioEssentials(biology,map,new MarketMap(biology));

    }

    /**
     * called shortly after the essentials are set, it is time now to return a list of all the agents
     *
     * @param model the model
     * @return a list of agents
     */
    @Override
    public ScenarioPopulation populateModel(FishState model) {


        return new ScenarioPopulation(new ArrayList<>(),new SocialNetwork(new EmptyNetworkBuilder()),null );
    }


    public int getNumberOfSpecies() {
        return numberOfSpecies;
    }

    public void setNumberOfSpecies(int numberOfSpecies) {
        this.numberOfSpecies = numberOfSpecies;
    }


    /**
     * Getter for property 'gridWidth'.
     *
     * @return Value for property 'gridWidth'.
     */
    public int getGridWidth() {
        return gridWidth;
    }

    /**
     * Setter for property 'gridWidth'.
     *
     * @param gridWidth Value to set for property 'gridWidth'.
     */
    public void setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;
    }


}



