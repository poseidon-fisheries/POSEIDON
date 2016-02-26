package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Point;
import ec.util.MersenneTwisterFast;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.Grid2D;
import sim.field.grid.ObjectGrid2D;
import sim.util.Bag;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.weather.initializer.WeatherInitializer;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.geography.pathfinding.Pathfinder;
import uk.ac.ox.oxfish.geography.pathfinding.StraightLinePathfinder;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.GISReaders;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A bunch of static methods that build maps. I wonder if I should just hide this within the calling scenarios, but maybe
 * they'll come in handy as separate methods at some point.
 * Created by carrknight on 4/10/15.
 */
public class NauticalMapFactory {



    /**
     * todo move to parameter list
     */
    final static private String DEFAULT_BATHYMETRY_SOURCE = "california1000.asc";


    /**
     * california MPA
     */
    final static private String[] DEFAULT_MPA_SOURCES = {"cssr_mpa/reprojected/mpa_central.shp",
            "ncssr_mpa/reprojected/mpa_north.shp"};

    public static NauticalMap fromBathymetryAndShapeFiles(
            Pathfinder pathfinder, String bathymetryResource, String... mpaSources)
    {
        //read raster bathymetry
        GeomGridField temporaryField = GISReaders.readRaster(bathymetryResource);
        DoubleGrid2D temporaryGrid = (DoubleGrid2D)temporaryField.getGrid(); //cast cast cast. Welcome to mason
        //now turn it into a grid of sea tiles
        ObjectGrid2D rasterBackingGrid = new ObjectGrid2D(temporaryField.getGridWidth(),temporaryField.getGridHeight());
        for(int i=0;i<rasterBackingGrid.getWidth(); i++)
            for(int j=0; j<rasterBackingGrid.getHeight(); j++)
                rasterBackingGrid.field[i][j]=new SeaTile(i,j,temporaryGrid.field[i][j], new TileHabitat(0d));
        //now from this grid create the correct bathymetry object
        GeomGridField rasterBathymetry = new GeomGridField(rasterBackingGrid);
        rasterBathymetry.setPixelHeight(temporaryField.getPixelHeight());
        rasterBathymetry.setPixelWidth(temporaryField.getPixelWidth());
        rasterBathymetry.setMBR(temporaryField.getMBR());



        //read in MPAs
        GeomVectorField mpaVectorField;
        if(mpaSources.length > 0)
            mpaVectorField = GISReaders.readShapeAndMergeWithRaster(rasterBathymetry, mpaSources);
        else
            mpaVectorField = new GeomVectorField();

        EquirectangularDistance distance = new EquirectangularDistance(temporaryField.toXCoord(0.5),
                                                                       temporaryField.getPixelHeight());


        NauticalMap map = new NauticalMap(rasterBathymetry, mpaVectorField, distance,pathfinder);
        for(SeaTile tile : map.getAllSeaTilesAsList())
            tile.setBiology(new EmptyLocalBiology());
        return map;




    }


    public static NauticalMap readBathymetryAndLowerItsResolution(
            final int gridWidth, final int gridHeight, final Path pathToBathymetryFile)
    {
        //read raster bathymetry
        GeomGridField temporaryField = GISReaders.readRaster(
                FishStateUtilities.getAbsolutePath(
                        pathToBathymetryFile.toString()));
        DoubleGrid2D temporaryGrid = (DoubleGrid2D)temporaryField.getGrid(); //cast cast cast. Welcome to mason
        ObjectGrid2D rasterBackingGrid = new ObjectGrid2D(gridWidth, gridHeight);
        //put a list of altitudes in each
        for(int i=0;i<rasterBackingGrid.getWidth(); i++)
            for(int j=0; j<rasterBackingGrid.getHeight(); j++) {
                rasterBackingGrid.field[i][j] = new ArrayList<Double>();
            }
        //now from this grid create the correct bathymetry object
        GeomGridField rasterBathymetry = new GeomGridField(rasterBackingGrid);
        rasterBathymetry.setPixelHeight(temporaryField.getPixelHeight());
        rasterBathymetry.setPixelWidth(temporaryField.getPixelWidth());
        rasterBathymetry.setMBR(temporaryField.getMBR());


        //put together the california map as if the original grid was made up actually by samples over the coarser
        //grid we are making!
        for(int i=0;i<temporaryGrid.getWidth(); i++)
            for(int j=0; j<temporaryGrid.getHeight(); j++)
            {
                int gridX = rasterBathymetry.toXCoord(temporaryField.toPoint(i,j));
                int gridY = rasterBathymetry.toYCoord(temporaryField.toPoint(i,j));
                System.out.println(gridX+","+gridY+","+temporaryGrid.field[i][j]);
                ((List) rasterBackingGrid.field[gridX][gridY]).add(temporaryGrid.field[i][j]);
            }

        //now turn it into seatiles
        for(int i=0;i<gridWidth; i++)
            for(int j=0; j<gridHeight; j++) {
                OptionalDouble average = ((List<Double>) rasterBackingGrid.field[i][j]).stream().filter(
                        aDouble -> aDouble > -9999).mapToDouble(value -> value).average();
                //if there was no observation, put a mountain there!
                double altitude = average.isPresent() ? average.getAsDouble() : -1000;
                rasterBackingGrid.field[i][j] = new SeaTile(i,j,altitude,new TileHabitat(0));
            }

        //read in MPAs
        GeomVectorField mpaVectorField = new GeomVectorField();

        EquirectangularDistance distance = new EquirectangularDistance(temporaryField.toXCoord(0.5),
                                                                       temporaryField.getPixelHeight());


        NauticalMap map = new NauticalMap(rasterBathymetry, mpaVectorField, distance, new StraightLinePathfinder());
        for(SeaTile tile : map.getAllSeaTilesAsList())
            tile.setBiology(new EmptyLocalBiology());
        return map;
    }


    /**
     * creates a map like the NETLOGO prototype. That's a 50x50 map
     */
    public static NauticalMap prototypeMap(
            Pathfinder pathfinder, int coastalRoughness,
            MersenneTwisterFast random,
            int depthSmoothing, final int width, final int height)
    {

        //build the grid
        ObjectGrid2D baseGrid =  new ObjectGrid2D(width, height);

        //the 10 rightmost patches are land, the rest is sea
        int landX = 10;
        if(width <= 10)
            landX = (int) Math.ceil(width *.2);

        for(int x=0; x< width; x++)
            for(int y=0; y< height; y++) {
                baseGrid.field[x][y] = x <width- landX ?
                        new SeaTile(x,y,-random.nextInt(5000), new TileHabitat(0d)) :
                        new SeaTile(x,y,2000, new TileHabitat(0d));
            }
        /***
         *       ___              _        _   ___               _
         *      / __|___  __ _ __| |_ __ _| | | _ \___ _  _ __ _| |_  _ _  ___ ______
         *     | (__/ _ \/ _` (_-<  _/ _` | | |   / _ \ || / _` | ' \| ' \/ -_|_-<_-<
         *      \___\___/\__,_/__/\__\__,_|_| |_|_\___/\_,_\__, |_||_|_||_\___/__/__/
         *                                                 |___/
         */
        if(landX >=10)
            for(int i=0; i<coastalRoughness; i++) {
                //now go roughen up the coast
                List<SeaTile> toFlip = new LinkedList<>();
                //go through all the tiles
                for (int x = 0; x < width; x++)
                    for (int y = 0; y < height; y++) {
                        SeaTile tile = (SeaTile) baseGrid.field[x][y];
                        if (tile.getAltitude() < 0)
                            continue; //if it's ocean, don't bother

                        Bag neighbors = new Bag();
                        baseGrid.getMooreNeighbors(x, y, 1, Grid2D.BOUNDED, false, neighbors, null, null);
                        //count how many neighbors are ocean
                        int seaNeighbors = 0;
                        for (Object neighbor : neighbors) {
                            if (((SeaTile) neighbor).getAltitude() < 0)
                                seaNeighbors++;
                        }
                        //if it has at least one neighbor, 40% chance of turning into sea
                        if (seaNeighbors > 0 && random.nextBoolean(.4))
                            toFlip.add(tile);
                    }
                //remove all the marked land tiles and turn them into ocean
                for (SeaTile toRemove : toFlip) {
                    assert toRemove.getAltitude() >= 0; //should be removing land!
                    SeaTile substitute = new SeaTile(toRemove.getGridX(), toRemove.getGridY(), -random.nextInt(5000),
                                                     new TileHabitat(0d));
                    assert baseGrid.field[toRemove.getGridX()][toRemove.getGridY()] == toRemove;
                    baseGrid.field[toRemove.getGridX()][toRemove.getGridY()] = substitute;
                }
            }

        /***
         *      ___                _   _    _
         *     / __|_ __  ___  ___| |_| |_ (_)_ _  __ _
         *     \__ \ '  \/ _ \/ _ \  _| ' \| | ' \/ _` |
         *     |___/_|_|_\___/\___/\__|_||_|_|_||_\__, |
         *                                        |___/
         */


        for(int i=0; i<depthSmoothing; i++)
        {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            SeaTile toChange = (SeaTile) baseGrid.get(x,y);
            x += random.nextInt(3)-1; x= Math.max(0,x); x = Math.min(x,width-1);
            y += random.nextInt(3)-1; y= Math.max(0, y); y = Math.min(y,height-1);
            SeaTile fixed = (SeaTile) baseGrid.get(x,y);
            double newAltitude = toChange.getAltitude() +
                    (random.nextDouble()*.04) *
                            (fixed.getAltitude()-toChange.getAltitude());
            if(newAltitude <0 && toChange.getAltitude() > 0)
                newAltitude = 1;
            if(newAltitude >0 && toChange.getAltitude() < 0)
                newAltitude = -1;

            //put the new one in!
            baseGrid.set(toChange.getGridX(),toChange.getGridY(),
                         new SeaTile(toChange.getGridX(),toChange.getGridY(),newAltitude, new TileHabitat(0d)));


        }







        GeomGridField bathymetry = new GeomGridField(baseGrid);
        GeomVectorField mpas = new GeomVectorField(baseGrid.getWidth(),baseGrid.getHeight()); //empty MPAs

        //expand MBR to cointan all the cells
        Point coordinates = bathymetry.toPoint(width-1, height-1);
        bathymetry.getMBR().expandToInclude(coordinates.getX(),coordinates.getY());
        coordinates = bathymetry.toPoint(width-1, 0);
        bathymetry.getMBR().expandToInclude(coordinates.getX(),coordinates.getY());
        coordinates = bathymetry.toPoint(0, 0);
        bathymetry.getMBR().expandToInclude(coordinates.getX(),coordinates.getY());
        coordinates = bathymetry.toPoint(0, height-1);
        bathymetry.getMBR().expandToInclude(coordinates.getX(), coordinates.getY());

        //expand it a little further
        bathymetry.getMBR().expandToInclude(bathymetry.getMBR().getMaxX()-bathymetry.getPixelWidth()/2,
                                            bathymetry.getMBR().getMaxY()-bathymetry.getPixelHeight()/2);
        bathymetry.getMBR().expandToInclude(bathymetry.getMBR().getMinX()-bathymetry.getPixelWidth()/2,
                                            bathymetry.getMBR().getMinY()-bathymetry.getPixelHeight()/2);

        mpas.setMBR(bathymetry.getMBR());

        Distance distance = new EquirectangularDistance(0,1);
        return new NauticalMap(bathymetry,mpas,distance,pathfinder);



    }

    /**
     * calls prototypeMap and adds a local biology and then smooths it a bit
     * @return the nautical map
     */
    public static NauticalMap prototypeMapWithRandomSmoothedBiology(
            int coastalRoughness,
            MersenneTwisterFast random,
            int depthSmoothing,
            final int width,
            final int height){

        return prototypeMap(new StraightLinePathfinder(), coastalRoughness,random,depthSmoothing, width, height);




    }


    public static NauticalMap initializeMap(NauticalMap map,
                                            MersenneTwisterFast random,
                                            BiologyInitializer biologyInitializer,
                                            WeatherInitializer weatherInitializer,
                                            GlobalBiology biology, FishState model)
    {

        //map.initializeBiology(RandomConstantBiologyInitializer(random,minBiomass,maxBiomass));;
        map.initializeBiology(biologyInitializer,random ,biology );
        /***
         *      ___                _   _      ___ _     _
         *     / __|_ __  ___  ___| |_| |_   | _ |_)___| |___  __ _ _  _
         *     \__ \ '  \/ _ \/ _ \  _| ' \  | _ \ / _ \ / _ \/ _` | || |
         *     |___/_|_|_\___/\___/\__|_||_| |___/_\___/_\___/\__, |\_, |
         *                                                    |___/ |__/
         */
        biologyInitializer.processMap(biology,map,random,model );
        weatherInitializer.processMap(map,random,model);

        return map;
    }

    /**
     * simple biology smoother that works with constant local biologies. This is a very confusing object,
     * but basically you give it a few parameters and it returns a Function. When you give this function the randomizer
     * it will return a Consumer which is basically the smoother. Give the smoother the map and it will smooth it for you
     * @param biologySmoothing how much biology smoothing to do
     * @param width width
     * @param height height
     * @return the smoother
     */
    public static Function<MersenneTwisterFast,Consumer<NauticalMap>>  smoothConstantBiology(
            int biologySmoothing, int width, int height) {
        return new Function<MersenneTwisterFast, Consumer<NauticalMap>>() {
            @Override
            public Consumer<NauticalMap> apply(MersenneTwisterFast random) {
                return new Consumer<NauticalMap>() {
                    @Override
                    public void accept(NauticalMap map) {
                        ObjectGrid2D baseGrid = (ObjectGrid2D) map.getRasterBathymetry().getGrid();
                        for (int i = 0; i < biologySmoothing; i++) {
                            int x = random.nextInt(width);
                            int y = random.nextInt(height);
                            SeaTile toChange = (SeaTile) baseGrid.get(x, y);
                            if (toChange.getAltitude() > 0) //land is cool man
                            {
                                assert toChange.getBiomass(null) <= 0;
                                continue;
                            }
                            x += random.nextInt(3) - 1;
                            x = Math.max(0, x);
                            x = Math.min(x, width-1);
                            y += random.nextInt(3) - 1;
                            y = Math.max(0, y);
                            y = Math.min(y, height-1);
                            SeaTile fixed = (SeaTile) baseGrid.get(x, y);
                            //null is not a specie but we know that the map is filled with constant biology so we are in the clear
                            double newBiology = Math.round(toChange.getBiomass(null) +
                                                                   (random.nextFloat() * .025f) *
                                                                           (fixed.getBiomass(null) - toChange.getBiomass(
                                                                                   null)));
                            if (newBiology <= 0)
                                newBiology = 1;

                            //put the new one in!
                            toChange.setBiology(new ConstantLocalBiology(newBiology));

                        }
                    }
                };
            }
        };


    }

    /**
     * add random ports to the map
     * @param map
     */
    public static void addRandomPortsToMap(NauticalMap map,int ports,
                                           Function<SeaTile,MarketMap> marketFactory,
                                           MersenneTwisterFast random){
        /***
         *        _      _    _   ___         _
         *       /_\  __| |__| | | _ \___ _ _| |_ ___
         *      / _ \/ _` / _` | |  _/ _ \ '_|  _(_-<
         *     /_/ \_\__,_\__,_| |_| \___/_|  \__/__/
         *
         */
        ObjectGrid2D baseGrid = (ObjectGrid2D) map.getRasterBathymetry().getGrid();
        int width = baseGrid.getWidth();
        int height = baseGrid.getHeight();

        ArrayList<SeaTile> candidateTiles = new ArrayList<>();
        for(int x=0; x<width; x++)
            for(int y=0; y<height; y++)
            {

                SeaTile possible = (SeaTile) baseGrid.get(x, y);
                if(possible.getAltitude() <= 0) //sea tiles aren't welcome!
                    continue;
                int neighboringSeaTiles = 0;
                Bag neighbors = new Bag();
                baseGrid.getMooreNeighbors(x,y,1,Grid2D.BOUNDED,false,neighbors,null,null);
                for(Object neighbor : neighbors)
                    if(((SeaTile)neighbor).getAltitude() < 0 )
                        neighboringSeaTiles++;

                if(neighboringSeaTiles >=Math.min(height,3))
                    candidateTiles.add(possible);

            }
        //get all candidates (land tiles with at least 4 sea tiles next to them)

        Collections.shuffle(candidateTiles,new Random(random.nextLong()));
        for(int i=0; i<ports; i++) {
            Port port = new Port("Port 0", candidateTiles.get(i), marketFactory.apply(candidateTiles.get(i)), 0);
            map.addPort(port);
        }

    }








    public static Function<SeaTile,LocalBiology> randomMultipleSpecies(MersenneTwisterFast random,
                                                                       int max, int numberOfSpecies)
    {
        assert numberOfSpecies > 1;

        return seaTile -> {
            if (seaTile.getAltitude() > 0)
                return new EmptyLocalBiology();
            else {
                double[] biomass = new double[numberOfSpecies];
                for(int i=0; i<biomass.length; i++)
                    biomass[i]=random.nextDouble()*max;
                return new ConstantHeterogeneousLocalBiology(biomass);
            }
        };

    }


    public static Map<SeaTile,double[]> getSpeciesForEachCellFromData(Map<Species,String> filenamesForSpecie,
                                                                      NauticalMap map)
    {
        //number of species
        int species = filenamesForSpecie.keySet().size();
        //list of all the tiles
        final List<SeaTile> tiles = map.getAllSeaTilesAsList();
        //altitude map
        final GeomGridField bathymetry = map.getRasterBathymetry();
        //map of results to return
        Map<SeaTile,double[]> toReturn = new HashMap<>();

        //we go for each specie/file since that's probably the largest thing to keep in memory
        for(Map.Entry<Species,String> file : filenamesForSpecie.entrySet())
        {
            final GeomGridField biomass = GISReaders.readRaster(file.getValue());

            for(SeaTile tile : tiles)
            {
                //allocate memory
                toReturn.putIfAbsent(tile,new double[species]);
                //get the coordinates of the seatile
                final Point tileCoordinate = bathymetry.toPoint(tile.getGridX(), tile.getGridY());
                //find which biomass grid it corresponds to
                int biomassX = biomass.toXCoord(tileCoordinate);
                int biomassY = biomass.toYCoord(tileCoordinate);
                //get it!
                final DoubleGrid2D grid = (DoubleGrid2D) biomass.getGrid();
                //as long as you are in the grid boundaries
                if(biomassY >= 0 && biomassY < biomass.getGridHeight()
                        &&
                        biomassX >= 0 && biomassX < biomass.getGridWidth()) {
                    final double tons = grid.get(biomassX, biomassY);
                    if(tons >=0 && Double.isFinite(tons)) //negative means no observation
                        toReturn.get(tile)[file.getKey().getIndex()]=tons+1;
                }

            }
        }

        return toReturn;
    }

}
