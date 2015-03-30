package uk.ac.ox.oxfish.model;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.geo.GeomGridField;
import uk.ac.ox.oxfish.utility.AscRasterReader;

/**
 *
 * The main model object. Like all the other simstates it holds the reference
 * to schedule and randomizer
 * Created by carrknight on 3/29/15.
 */
public class FishState  extends SimState{

    /**
     * this holds the bathymetry raster grid
     */
    private  GeomGridField rasterBathymetry;



    public FishState(long seed) {
        super(seed);
    }


    /**
     * so far it does the following:
     *  * read in the data into a the raster
     */
    @Override
    public void start() {
        super.start();


        //read raster bathymetry
        rasterBathymetry = AscRasterReader.read("california1000.asc");


        //schedule to print repeatedly the day
        schedule.scheduleRepeating(simState ->
                System.out.println("the time is " + simState.schedule.getTime()));


    }


    public GeomGridField getRasterBathymetry() {
        return rasterBathymetry;
    }
}
