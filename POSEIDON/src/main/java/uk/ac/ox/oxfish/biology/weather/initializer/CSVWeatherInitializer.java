/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.biology.weather.initializer;

import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import org.jfree.util.Log;
import uk.ac.ox.oxfish.biology.weather.ConstantWeather;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.CsvColumnToList;
import uk.ac.ox.oxfish.utility.TimeSeriesActuator;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.LinkedList;

/**
 * same wind speed  time series for everyone, read in through the given CSV and stepped through the actuator
 * Created by carrknight on 11/29/16.
 */
public class CSVWeatherInitializer implements WeatherInitializer {


    private final CsvColumnToList list;
    private final DoubleParameter temperature = new FixedDoubleParameter(30);
    private TimeSeriesActuator actuator;

    public CSVWeatherInitializer(
        final CsvColumnToList list
    ) {
        this.list = list;
    }

    @Override
    public void processMap(
        final NauticalMap map, final MersenneTwisterFast random, final FishState state
    ) {

        //create the object proper
        final ConstantWeather weather =
            new ConstantWeather(
                temperature.applyAsDouble(state.getRandom()),
                -1, 0
            );

        //apply it to all the seatiles
        for (final SeaTile seaTile : map.getAllSeaTilesAsList())
            seaTile.assignLocalWeather(weather);

        //create the actuator from the csv column read


        final LinkedList<Double> windSpeeds = list.readColumn();

        //create an actuator to change speed every day
        actuator = TimeSeriesActuator.weatherDailySchedule(
            windSpeeds,
            Lists.newArrayList(weather)
        );


        if (Log.isDebugEnabled())
            Log.debug("read " + windSpeeds.size() + " wind speeds");
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
