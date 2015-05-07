package uk.ac.ox.oxfish.geography;

import ec.util.MersenneTwisterFast;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.Grid2D;
import sim.field.grid.ObjectGrid2D;
import sim.util.Bag;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.model.market.Markets;
import uk.ac.ox.oxfish.utility.GISReaders;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A bunch of static methods that build maps. I wonder if I should just hide this within the calling scenarios, but maybe
 * they'll come in handy as separate methods at some point.
 * Created by carrknight on 4/10/15.
 */
public class NauticalMapFactory {


    public static NauticalMap fromBathymetryAndShapeFiles(String bathymetryResource, String... mpaSources)
    {
        //read raster bathymetry
        GeomGridField temporaryField = GISReaders.readRaster(bathymetryResource);
        DoubleGrid2D temporaryGrid = (DoubleGrid2D)temporaryField.getGrid(); //cast cast cast. Welcome to mason
        //now turn it into a grid of sea tiles
        ObjectGrid2D rasterBackingGrid = new ObjectGrid2D(temporaryField.getGridWidth(),temporaryField.getGridHeight());
        for(int i=0;i<rasterBackingGrid.getWidth(); i++)
            for(int j=0; j<rasterBackingGrid.getHeight(); j++)
                rasterBackingGrid.field[i][j]=new SeaTile(i,j,temporaryGrid.field[i][j]);
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

        return new NauticalMap(rasterBathymetry,mpaVectorField,distance);



    }


    /**
     * creates a map like the NETLOGO prototype. That's a 50x50 map
     */
    public static NauticalMap prototypeMap(
            int coastalRoughness,
            MersenneTwisterFast random,
            int depthSmoothing)
    {

        //build the grid
        ObjectGrid2D baseGrid =  new ObjectGrid2D(50, 50);

        //the 10 rightmost patches are land, the rest is sea
        for(int x=0; x< 50; x++)
            for(int y=0; y< 50; y++)
                baseGrid.field[x][y] = x <40 ?
                        new SeaTile(x,y,-random.nextInt(5000)) :
                        new SeaTile(x,y,2000);
        /***
         *       ___              _        _   ___               _
         *      / __|___  __ _ __| |_ __ _| | | _ \___ _  _ __ _| |_  _ _  ___ ______
         *     | (__/ _ \/ _` (_-<  _/ _` | | |   / _ \ || / _` | ' \| ' \/ -_|_-<_-<
         *      \___\___/\__,_/__/\__\__,_|_| |_|_\___/\_,_\__, |_||_|_||_\___/__/__/
         *                                                 |___/
         */
        for(int i=0; i<coastalRoughness; i++) {
            //now go roughen up the coast
            List<SeaTile> toFlip = new LinkedList<>();
            //go through all the tiles
            for (int x = 0; x < 50; x++)
                for (int y = 0; y < 50; y++) {
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
                assert toRemove.getAltitude() > 0; //should be removing land!
                SeaTile substitute = new SeaTile(toRemove.getGridX(), toRemove.getGridY(), -random.nextInt(5000));
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
            int x = random.nextInt(50);
            int y = random.nextInt(50);
            SeaTile toChange = (SeaTile) baseGrid.get(x,y);
            x += random.nextInt(3)-1; x= Math.max(0,x); x = Math.min(x,49);
            y += random.nextInt(3)-1; y= Math.max(0, y); y = Math.min(y,49);
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
                    new SeaTile(toChange.getGridX(),toChange.getGridY(),newAltitude));


        }







        GeomGridField bathymetry = new GeomGridField(baseGrid);
        GeomVectorField mpas = new GeomVectorField(); //empty MPAs
        Distance distance = new EquirectangularDistance(0,1);
        return new NauticalMap(bathymetry,mpas,distance);



    }

    /**
     * calls prototypeMap and adds a local biology and then smooths it a bit
     * @return the nautical map
     */
    public static NauticalMap prototypeMapWithRandomSmoothedBiology(
            int coastalRoughness,
            MersenneTwisterFast random,
            int depthSmoothing,
            Function<SeaTile, LocalBiology> biologyInitializer,
            Consumer<NauticalMap> biologySmoother){

        NauticalMap map = prototypeMap(coastalRoughness,random,depthSmoothing);

        //map.initializeBiology(randomConstantBiology(random,minBiomass,maxBiomass));;
        map.initializeBiology(biologyInitializer);;
        /***
         *      ___                _   _      ___ _     _
         *     / __|_ __  ___  ___| |_| |_   | _ |_)___| |___  __ _ _  _
         *     \__ \ '  \/ _ \/ _ \  _| ' \  | _ \ / _ \ / _ \/ _` | || |
         *     |___/_|_|_\___/\___/\__|_||_| |___/_\___/_\___/\__, |\_, |
         *                                                    |___/ |__/
         */
        biologySmoother.accept(map);

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
                            x = Math.min(x, 49);
                            y += random.nextInt(3) - 1;
                            y = Math.max(0, y);
                            y = Math.min(y, 49);
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
                                           Function<SeaTile,Markets> marketFactory,
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

                if(neighboringSeaTiles >=4)
                    candidateTiles.add(possible);

            }
        //get all candidates (land tiles with at least 4 sea tiles next to them)

        Collections.shuffle(candidateTiles,new Random(random.nextLong()));
        for(int i=0; i<ports; i++) {
            Port port = new Port(candidateTiles.get(i),marketFactory.apply(candidateTiles.get(i)) );
            map.addPort(port);
        }

    }



 /*   public GeomVectorField addCities(GeomGridField rasterBathymetry, String cityResources)
    {

        GeomVectorField cities = GISReaders.readShapeAndMergeWithRaster(rasterBathymetry,cityResources);
        //now transform the MasonGeometries into Cities
        Envelope savedMBR = new Envelope(cities.getMBR());
        Bag oldGeometries = new Bag(cities.getGeometries());
        cities.getGeometries().clear();
        for(Object old : oldGeometries)
        {
            MasonGeometry geometry = (MasonGeometry) old;

            cities.addGeometry(new City(geometry.getGeometry(),geometry.getStringAttribute("AREANAME"),
                    geometry.getIntegerAttribute("POP2000")));
        }

        return cities;

    }
    */


    /**
     * confusing? A function that returns an initialization function
     * @param random randomizer
     * @param min minimum biomass
     * @param max maximum biomass
     * @return
     */
    public static Function<SeaTile,LocalBiology> randomConstantBiology(MersenneTwisterFast random,
                                                                       int min, int max)
    {
        assert max > min;

        return seaTile -> {
            if (seaTile.getAltitude() > 0)
                return new EmptyLocalBiology();
            else
                return new ConstantLocalBiology(random.nextInt(max - min) + min);
        };
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

    public static Function<SeaTile,LocalBiology> fromLeftToRightBiology(int max,int width)
    {
        assert  width > 0;

        return seaTile -> {
            if (seaTile.getAltitude() > 0)
                return new EmptyLocalBiology();
            else
                return new ConstantLocalBiology(max*Math.pow((1-seaTile.getGridX()/(double)width),2));
        };
    }
}
