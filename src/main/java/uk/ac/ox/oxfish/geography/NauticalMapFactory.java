package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Envelope;
import ec.util.MersenneTwisterFast;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.Grid2D;
import sim.field.grid.ObjectGrid2D;
import sim.util.Bag;
import uk.ac.ox.oxfish.biology.ConstantLocalBiology;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.utility.GISReaders;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * A bunch of static methods that build maps
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
        GeomVectorField mpaVectorField = GISReaders.readShapeAndMergeWithRaster(rasterBathymetry, mpaSources);

        EquirectangularDistance distance = new EquirectangularDistance(temporaryField.toXCoord(0.5),
                temporaryField.getPixelHeight());

        return new NauticalMap(rasterBathymetry,mpaVectorField,distance);



    }


    /**
     * creates a map like the NETLOGO prototype. That's a 50x50 map
     */
    public static NauticalMap prototypeMap(int coastalRoughness,
                                           MersenneTwisterFast random,
                                           int smoothingNumber)
    {

        //build the grid
        ObjectGrid2D baseGrid =  new ObjectGrid2D(50,50);

        //the 10 rightmost patches are land, the rest is sea
        for(int x=0; x<50; x++)
            for(int y=0; y<50; y++)
                baseGrid.field[x][y] = x <40 ?
                        new SeaTile(x,y,-5000) :
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
                SeaTile substitute = new SeaTile(toRemove.getGridX(), toRemove.getGridY(), -5000);
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

        for(int i=0; i<smoothingNumber; i++)
        {
            int x = random.nextInt(50);
            int y = random.nextInt(50);
            SeaTile toChange = (SeaTile) baseGrid.get(x,y);
            x += random.nextInt(3)-1; x= Math.max(0,x); x = Math.min(x,49);
            y += random.nextInt(3)-1; y= Math.max(0, y); y = Math.min(y,49);
            SeaTile fixed = (SeaTile) baseGrid.get(x,y);
            double newAltitude = toChange.getAltitude() +
                    (random.nextDouble()*.05) *
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
     * calls prototypeMap and adds a randomized biology and then smooths it a bit
     * @return the nautical map
     */
    public static NauticalMap prototypeMapWithRandomSmoothedBiology(int coastalRoughness,
                                                                    MersenneTwisterFast random,
                                                                    int smoothingNumber,
                                                                    int minBiomass,
                                                                    int maxBiomass){

        NauticalMap map = prototypeMap(coastalRoughness,random,smoothingNumber);

        map.initializeBiology(randomConstantBiology(random,minBiomass,maxBiomass));;
        /***
         *      ___                _   _      ___ _     _
         *     / __|_ __  ___  ___| |_| |_   | _ |_)___| |___  __ _ _  _
         *     \__ \ '  \/ _ \/ _ \  _| ' \  | _ \ / _ \ / _ \/ _` | || |
         *     |___/_|_|_\___/\___/\__|_||_| |___/_\___/_\___/\__, |\_, |
         *                                                    |___/ |__/
         */
        ObjectGrid2D baseGrid = (ObjectGrid2D) map.getRasterBathymetry().getGrid();
        for(int i=0; i<smoothingNumber; i++)
        {
            int x = random.nextInt(50);
            int y = random.nextInt(50);
            SeaTile toChange = (SeaTile) baseGrid.get(x,y);
            if(toChange.getAltitude() > 0) //land is cool man
            {
                assert toChange.getBiomass(null) <=0;
                continue;
            }
            x += random.nextInt(3)-1; x= Math.max(0,x); x = Math.min(x,49);
            y += random.nextInt(3)-1; y= Math.max(0, y); y = Math.min(y,49);
            SeaTile fixed = (SeaTile) baseGrid.get(x,y);
            //null is not a specie but we know that the map is filled with constant biology so we are in the clear
            int newBiology = Math.round(toChange.getBiomass(null) +
                    (random.nextFloat()*.05f) *
                            (fixed.getBiomass(null)-toChange.getBiomass(null)));
            if(newBiology <=0)
                newBiology = 1;

            //put the new one in!
            toChange.setBiology(new ConstantLocalBiology(newBiology));


        }

        return map;



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
     * @param random
     * @param min
     * @param max
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

}
