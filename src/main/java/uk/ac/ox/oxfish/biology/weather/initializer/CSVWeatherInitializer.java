package uk.ac.ox.oxfish.biology.weather.initializer;

import com.google.common.collect.Lists;
import com.opencsv.CSVReader;
import ec.util.MersenneTwisterFast;
import org.jfree.util.Log;
import uk.ac.ox.oxfish.biology.weather.ConstantWeather;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.CsvColumnToList;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.TimeSeriesActuator;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * same wind speed  time series for everyone, read in through the given CSV and stepped through the actuator
 * Created by carrknight on 11/29/16.
 */
public class CSVWeatherInitializer implements WeatherInitializer {



    private TimeSeriesActuator actuator;

    private final CsvColumnToList list;


    public CSVWeatherInitializer(
            CsvColumnToList list) {
        this.list = list;
    }

    private final DoubleParameter temperature = new FixedDoubleParameter(30);


    @Override
    public void processMap(
            NauticalMap map, MersenneTwisterFast random, FishState state) {

        //create the object proper
        ConstantWeather weather =
                new ConstantWeather(
                        temperature.apply(state.getRandom()),
                        -1,0
                );

        //apply it to all the seatiles
        for(SeaTile seaTile : map.getAllSeaTilesAsList())
            seaTile.assignLocalWeather(weather);

        //create the actuator from the csv column read


        LinkedList<Double> windSpeeds = list.readColumn();

        //create an actuator to change speed every day
        actuator = TimeSeriesActuator.weatherDailySchedule(
                windSpeeds,
                Lists.newArrayList(weather));


        if(Log.isDebugEnabled())
            Log.debug("read " + windSpeeds.size() +" wind speeds");
        //schedule it
        state.scheduleEveryDay(actuator, StepOrder.BIOLOGY_PHASE);
        //step it once!
        actuator.step(state);






    }


    /**
     * Getter for property 'actuator'.
     *
     * @return Value for property 'actuator'.
     */
    public TimeSeriesActuator getActuator() {
        return actuator;
    }
}
