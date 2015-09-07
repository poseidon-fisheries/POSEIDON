package uk.ac.ox.oxfish.biology.weather.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Called at the beginning of the simulation to give a local weather object to each seatile
 * Created by carrknight on 9/7/15.
 */
public interface WeatherInitializer {


    void processMap(NauticalMap map, MersenneTwisterFast random, FishState model);






}
