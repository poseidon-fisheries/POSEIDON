package uk.ac.ox.oxfish.model.scenario;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.ObjectGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.MultipleSpeciesAbundanceInitializer;
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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Reads the bathymetry file of california and for now not much else.
 * Created by carrknight on 5/7/15.
 */
public class CaliforniaBathymetryScenario implements Scenario {

    /**
     * how much should the model biomass/abundance be given the data we read in?
     */
    private final double biomassScaling = 1.0;
    private int gridWidth = 50;


    public CaliforniaBathymetryScenario() {


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
            Path mainDirectory = Paths.get("inputs", "california");
            Path bioDirectory = mainDirectory.resolve("biology");

            DirectoryStream<Path> folders = Files.newDirectoryStream(bioDirectory);
            LinkedHashMap<String,Path> spatialFiles = new LinkedHashMap<>();
            LinkedHashMap<String, Path> folderMap = new LinkedHashMap<>();


            //each folder is supposedly a species
            for(Path folder : folders)
            {

                Path file = folder.resolve("spatial.csv");
                if(file.toFile().exists())
                {
                    String name = folder.getFileName().toString();
                    spatialFiles.put(name, file);
                    Preconditions.checkArgument(folder.resolve("count.csv").toFile().exists(),
                                                "The folder "+ name +
                                                        "  doesn't contain the abundance count.csv");

                    Preconditions.checkArgument(folder.resolve("meristics.yaml").toFile().exists(),
                                                "The folder "+ name +
                                                        "  doesn't contain the abundance count.csv");

                    folderMap.put(folder.getFileName().toString(),folder);
                }
                else
                {
                    if(Log.WARN)
                        Log.warn(folder.getFileName() + " does not have a spatial.txt file and so cannot be distributed on the map. It will be ignored");
                }

            }


            MultipleSpeciesAbundanceInitializer initializer = new MultipleSpeciesAbundanceInitializer(folderMap,
                                                                                                      biomassScaling);


            SampledMap sampledMap = new SampledMap(Paths.get("inputs", "california",
                                                             "california.csv"),
                                                   gridWidth,
                                                   spatialFiles);

            //we want a grid of numbers but we have a grid where every cell has many observations
            int gridHeight = sampledMap.getGridHeight();
            ObjectGrid2D altitudeGrid = new ObjectGrid2D(gridWidth, gridHeight);
            ObjectGrid2D sampledAltitudeGrid = sampledMap.getAltitudeGrid();
            //so for altitude we just average them out
            for(int x=0;x<gridWidth;x++)
                for(int y=0;y<gridHeight;y++)
                {
                    OptionalDouble average = ((LinkedList<Double>) sampledAltitudeGrid.get(x, y)).
                            stream().mapToDouble(
                            value -> value).filter(
                            aDouble -> aDouble > -9999).average();
                    altitudeGrid.set(x, y,
                                     new SeaTile(x, y, average.orElseGet(() -> 1000d), new TileHabitat(0)));
                }

            biology = initializer.generateGlobal(model.getRandom(),
                                                 model);
            List<Species> species = biology.getSpecies();

            GeomGridField unitedMap = new GeomGridField(altitudeGrid);
            unitedMap.setMBR(sampledMap.getMbr());
            //create the map
            map = new NauticalMap(unitedMap, new GeomVectorField(),
                                  new CartesianDistance(1),
                                  new StraightLinePathfinder());
            //for all species, find the total observations you get

            //this table contains for each x-y an array telling for each specie what is the average observation at x,y
            final Table<Integer,Integer,double[]> averagesTable = HashBasedTable.create(gridWidth,gridHeight);
            //go through the map
            for(int x=0;x<gridWidth;x++) {
                for (int y = 0; y < gridHeight; y++) {
                    double[] averages = new double[species.size()];
                    averagesTable.put(x, y, averages);
                    SeaTile seaTile = map.getSeaTile(x, y);
                    seaTile.setBiology(initializer.generateLocal(biology,seaTile,model.getRandom(),gridHeight,gridWidth));
                    //if it's sea (don't bother counting otherwise)
                    if (seaTile.getAltitude() < 0) {
                        int i = 0;
                        //each specie grid value is an ObjectGrid2D whose cells are themselves list of observations
                        //for each species
                        for (Map.Entry<String, ObjectGrid2D> specieGrid : sampledMap.getBiologyGrids().entrySet()) {
                            assert species.get(i).getName().equals(specieGrid.getKey()); //check we got the correct one
                            //average
                            OptionalDouble average = ((LinkedList<Double>) specieGrid.getValue().get(x,
                                                                                                     y)).stream().mapToDouble(
                                    value -> value).average();
                            averages[i] = average.orElse(0);
                            i++;
                        }
                    }


                }
            }
            //now that we have the averages, we can compute their sum:
            final double[] sums = new double[species.size()];
            for(double[] average : averagesTable.values())
                for(int i=0; i<sums.length; i++)
                    sums[i] += average[i];

            //and now finally we can turn all that into allocators
            for(Species current : biology.getSpecies())
                initializer.putAllocator(current, input ->
                        (averagesTable.get(input.getGridX(), input.getGridY())[current.getIndex()])
                        /
                        sums[current.getIndex()]);

            initializer.processMap(biology,map,model.getRandom(),model);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Some files were missing!");
        }

        if(Log.TRACE)
            Log.trace("height: " +map.getHeight());
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



