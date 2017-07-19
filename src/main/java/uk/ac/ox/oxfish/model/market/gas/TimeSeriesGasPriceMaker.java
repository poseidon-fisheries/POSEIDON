package uk.ac.ox.oxfish.model.market.gas;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.utility.TimeSeriesActuator;

import java.util.LinkedList;
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
            List<Double> timeSeries, boolean startOver, IntervalPolicy interval) {
        Preconditions.checkArgument(timeSeries.size()>=2, "Gas price time series ought to be at least 2 numbers long!");
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
    public void start(Port port, FishState model)
    {

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
        model.schedulePerPolicy(actuator, StepOrder.POLICY_UPDATE,interval);


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
