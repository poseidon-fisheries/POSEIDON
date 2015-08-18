package uk.ac.ox.oxfish.fisher.selfanalysis;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.MovingAverage;
import uk.ac.ox.oxfish.model.data.MovingVariance;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.maximization.Sensor;

import java.util.function.Function;

/**
 * A predictor that simply returns the moving average as a predictor for a value and a normal distribution
 * CDF for probability prediction
 * Created by carrknight on 8/18/15.
 */
public class MovingAveragePredictor implements Predictor, Steppable{


    /**
     * name to use in the data column
     */
    private final String name;


    /**
     * this actually computes both
     */
    private final MovingVariance<Double> averager;

    /**
     * this we use to add to the averager
     */
    private final Sensor<Double> sensor;

    private Fisher fisher;

    private Stoppable stoppable;


    public MovingAveragePredictor(String name, Sensor<Double> sensor,int averageWindow) {
        this.name = name;
        this.sensor = sensor;
        averager = new MovingVariance<>(averageWindow);


    }

    @Override
    public void start(FishState model, Fisher fisher) {

        this.fisher = fisher;
        stoppable = model.scheduleEveryDay(this, StepOrder.INDIVIDUAL_DATA_GATHERING);

        //store your prediction:
        fisher.getDailyData().registerGatherer(name, fisher1 -> averager.getAverage(),Double.NaN);
    }

    @Override
    public void turnOff() {
        if(stoppable!= null)
            stoppable.stop();
    }


    @Override
    public void step(SimState simState)
    {

        averager.addObservation(sensor.scan(fisher));

    }

    /**
     * ask the predictor the expected value of the variable it is tracking
     *
     * @return the expected value
     */
    @Override
    public double predict() {
        return averager.getAverage();
    }

    /**
     * ask the predictor what is the probability the variable it is tracking is below a given level
     *
     * @param level the level
     * @return P(x < level)
     */
    @Override
    public double probabilityBelowThis(double level) {
        if(averager.getSmoothedObservation()==0)
            return level < averager.getAverage() ? 0 : 1;

        double normalized = (level-averager.getAverage())/Math.sqrt(averager.getSmoothedObservation());
        if(Double.isFinite(normalized))
            return FishStateUtilities.CNDF(normalized);
        else
            return Double.NaN;
    }
}
