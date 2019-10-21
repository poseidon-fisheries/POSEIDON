/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.geography.mapmakers;

import com.vividsolutions.jts.geom.Point;
import ec.util.MersenneTwisterFast;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.Grid2D;
import sim.field.grid.ObjectGrid2D;
import sim.util.Bag;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.geography.CartesianDistance;
import uk.ac.ox.oxfish.geography.Distance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.geography.pathfinding.Pathfinder;
import uk.ac.ox.oxfish.geography.pathfinding.StraightLinePathfinder;
import uk.ac.ox.oxfish.model.FishState;

import java.util.LinkedList;
import java.util.List;

/**
 * Creates the standard map we use for the prototype. Inspired by the westcoast, I am told.
 * Created by carrknight on 11/5/15.
 */
public class SimpleMapInitializer implements MapInitializer {


    private final int width;

    private final int height;

    private final int coastalRoughness;

    private final int depthSmoothing;

    private final  double cellSizeInKilometers;
    private final int maxLandWidth;

    public SimpleMapInitializer(
            int width, int height, int coastalRoughness, int depthSmoothing, double cellSizeInKilometers,
            int maxLandWidth) {
        this.width = width;
        this.height = height;
        this.coastalRoughness = coastalRoughness;
        this.depthSmoothing = depthSmoothing;
        this.cellSizeInKilometers = cellSizeInKilometers;
        this.maxLandWidth = maxLandWidth;
    }

    /**
     * creates the map. Because there are no real obstacles I am going to assume straight pathfinder and cartesian distance
     * @param random the randomizer
     * @param biology the biology
     * @param model the model itself
     * @return
     */
    public NauticalMap makeMap(MersenneTwisterFast random, GlobalBiology biology, FishState model)
    {
        //build the grid
        ObjectGrid2D baseGrid =  new ObjectGrid2D(width, height);

        //choose how much of the world is land
        int actualLandWidth = maxLandWidth;
        if(width <= 10)
            actualLandWidth = (int) Math.ceil(width *.2);

        for(int x=0; x< width; x++)
            for(int y=0; y< height; y++) {
                baseGrid.field[x][y] = x <width- actualLandWidth ?
                        new SeaTile(x, y, -random.nextInt(5000), new TileHabitat(0d)) :
                        new SeaTile(x,y,2000, new TileHabitat(0d));
            }
        /***
         *       ___              _        _   ___               _
         *      / __|___  __ _ __| |_ __ _| | | _ \___ _  _ __ _| |_  _ _  ___ ______
         *     | (__/ _ \/ _` (_-<  _/ _` | | |   / _ \ || / _` | ' \| ' \/ -_|_-<_-<
         *      \___\___/\__,_/__/\__\__,_|_| |_|_\___/\_,_\__, |_||_|_||_\___/__/__/
         *                                                 |___/
         */
        if(actualLandWidth >=10)
            for(int i=0; i<coastalRoughness; i++) {
                //now go roughen up the coast
                List<SeaTile> toFlip = new LinkedList<>();
                //go through all the tiles
                for (int x = 0; x < width; x++)
                    for (int y = 0; y < height; y++) {
                        SeaTile tile = (SeaTile) baseGrid.field[x][y];
                        if (tile.isWater())
                            continue; //if it's ocean, don't bother

                        Bag neighbors = new Bag();
                        baseGrid.getMooreNeighbors(x, y, 1, Grid2D.BOUNDED, false, neighbors, null, null);
                        //count how many neighbors are ocean
                        int seaNeighbors = 0;
                        for (Object neighbor : neighbors) {
                            if (((SeaTile) neighbor).isWater())
                                seaNeighbors++;
                        }
                        //if it has at least one neighbor, 40% chance of turning into sea
                        if (seaNeighbors > 0 && random.nextBoolean(.4))
                            toFlip.add(tile);
                    }
                //remove all the marked land tiles and turn them into ocean
                for (SeaTile toRemove : toFlip) {
                    assert toRemove.isLand(); //should be removing land!
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
        GeomVectorField mpas = new GeomVectorField(baseGrid.getWidth(), baseGrid.getHeight()); //empty MPAs

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

        Distance distance = new CartesianDistance(cellSizeInKilometers);
        Pathfinder pathfinder = new StraightLinePathfinder();
        return new NauticalMap(bathymetry,mpas,distance,pathfinder);
    }


    public int getCoastalRoughness() {
        return coastalRoughness;
    }

    public int getDepthSmoothing() {
        return depthSmoothing;
    }

    public double getCellSizeInKilometers() {
        return cellSizeInKilometers;
    }
}
