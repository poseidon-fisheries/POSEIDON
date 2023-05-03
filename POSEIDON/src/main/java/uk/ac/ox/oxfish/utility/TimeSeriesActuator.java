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

package uk.ac.ox.oxfish.utility;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.weather.ConstantWeather;
import uk.ac.ox.oxfish.geography.ports.Port;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Basically you give it a time series and a method to call and it will call that method every time
 * it is stepped feeding it the next element of the time series.
 * When the time series is done, start over
 * Created by carrknight on 11/29/16.
 */
public class TimeSeriesActuator implements Steppable
{


    /**
     * list containing the elements to set
     */
    private final List<Double> timeSeries;

    /**
     * the function we are going to call
     */
    private final Consumer<Double> actuator;

    private Iterator<Double> iterator;

    /**
     * if we run out of things to read, do we start from the top?
     */
    private final boolean startOver;

    /**
     * helper static constructor for gas time series
     * @param gasSchedule time series of prices
     * @param ports ports to apply the gas price to
     * @return the actuator to schedule
     */
    public static TimeSeriesActuator gasPriceDailySchedule(
            LinkedList<Double> gasSchedule, List<Port> ports)
    {
        return new TimeSeriesActuator(
                gasSchedule,
                new Consumer<Double>() {
                    @Override
                    public void accept(Double nextPrice) {
                        for(Port port : ports)
                            port.setGasPricePerLiter(nextPrice);
                    }
                },
                true);

    }



    /**
     * helper static constructor for weather time series
     * @param gasSchedule time series of prices
     * @param weathers list of all the weather objects to update at once
     * @return the actuator to schedule
     */
    public static TimeSeriesActuator weatherDailySchedule(
            LinkedList<Double> gasSchedule, List<ConstantWeather> weathers)
    {
        return new TimeSeriesActuator(
                gasSchedule,
                new Consumer<Double>() {
                    @Override
                    public void accept(Double windSpeed) {
                        for(ConstantWeather weather : weathers)
                            weather.setWindSpeed(windSpeed);
                    }
                },
                true);

    }

    public TimeSeriesActuator(
            List<Double> timeSeries,
            Consumer<Double> actuator, boolean startOver) {
        this.startOver = startOver;
        Preconditions.checkArgument(timeSeries.size()>0);

        this.timeSeries = timeSeries;
        this.actuator = actuator;
    }


    /**
     * next element of the time series is sent to actuator
     */
    @Override
    public void step(SimState simState)
    {

        //starting or finished the list? start again!
        if(iterator == null)
            iterator= timeSeries.iterator();

        if(!iterator.hasNext()) {
            if (startOver)
                iterator = timeSeries.iterator();
            else
                return;
        }

        Double nextGasPrice = iterator.next();
        actuator.accept(nextGasPrice);

    }

}
