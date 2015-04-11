package uk.ac.ox.oxfish.model;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;

/**
 *
 * The main model object. Like all the other simstates it holds the reference
 * to schedule and randomizer
 * Created by carrknight on 3/29/15.
 */
public class FishState  extends SimState{


    private NauticalMap map;



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
      //  map = NauticalMap.initializeWithDefaultValues();
        map = NauticalMapFactory.prototypeMap(4,random,1000000);
  //      map.addCities("cities/cities.shp");






        //schedule to print repeatedly the day
        schedule.scheduleRepeating(new Steppable() {
            @Override
            public void step(SimState simState) {
                System.out.println("the time is " + simState.schedule.getTime());
            }
        });




    }


    public GeomGridField getRasterBathymetry() {
        return map.getRasterBathymetry();
    }

    public GeomVectorField getMpaVectorField() {
        return map.getMpaVectorField();
    }

    public GeomVectorField getCities() {
        return map.getCities();
    }
}
