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

package uk.ac.ox.oxfish.model.market.gas;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.utility.TimeSeriesActuator;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by carrknight on 7/18/17.
 */
public class TimeSeriesGasPriceMaker implements GasPriceMaker {


    /**
     * list containing the elements to set
     */
    private final List<Double> timeSeries;

    /**
     * if we run out of things to read, do we start from the top?
     */
    private final boolean startOver;


    private final IntervalPolicy interval;

    private final double initialPrice;


    public TimeSeriesGasPriceMaker(
        List<Double> timeSeries, boolean startOver, IntervalPolicy interval
    ) {
        Preconditions.checkArgument(
            timeSeries.size() >= 2,
            "Gas price time series ought to be at least 2 numbers long!"
        );
        this.initialPrice = timeSeries.remove(0);
        this.timeSeries = timeSeries;
        this.startOver = startOver;
        this.interval = interval;
    }

    @Override
    public double supplyInitialPrice(SeaTile location, String portName) {
        return initialPrice;
    }

    @Override
    public void start(Port port, FishState model) {

        TimeSeriesActuator actuator = new TimeSeriesActuator(
            timeSeries,
            new Consumer<Double>() {
                @Override
                public void accept(Double price) {
                    port.setGasPricePerLiter(price);
                }
            },
            startOver
        );
        model.schedulePerPolicy(actuator, StepOrder.POLICY_UPDATE, interval);


    }

    /**
     * Getter for property 'timeSeries'.
     *
     * @return Value for property 'timeSeries'.
     */
    public List<Double> getTimeSeries() {
        return timeSeries;
    }

    /**
     * Getter for property 'startOver'.
     *
     * @return Value for property 'startOver'.
     */
    public boolean isStartOver() {
        return startOver;
    }

    /**
     * Getter for property 'interval'.
     *
     * @return Value for property 'interval'.
     */
    public IntervalPolicy getInterval() {
        return interval;
    }
}
