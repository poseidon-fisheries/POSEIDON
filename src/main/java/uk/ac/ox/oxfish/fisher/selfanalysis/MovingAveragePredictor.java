package uk.ac.ox.oxfish.fisher.selfanalysis;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.MovingAverage;
import uk.ac.ox.oxfish.model.data.MovingVariance;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.maximization.Sensor;

import java.util.function.Function;

/**
 * A predictor that simply returns the moving average as a predictor for a value and a normal distribution
 * CDF for probability prediction. Instantiate through static methods rather than constructor
 * Created by carrknight on 8/18/15.
 */
public abstract class MovingAveragePredictor implements Predictor, Steppable{


    /**
     * name to use in the data column
     */
    protected final String name;


    /**
     * this actually computes both
     */
    protected final MovingVariance<Double> averager;

    /**
     * this we use to add to the averager
     */
    protected final Sensor<Double> sensor;

    protected Fisher fisher;

    protected Stoppable stoppable;


    /**
     * moving average predictor that calls up the sensor every day
     * @param name name of the column we will store the prediction in
     * @param sensor function to retrieve the daily observation
     * @param averageWindow length of the moving average
     * @return a concrete moving average
     */
    public static MovingAveragePredictor dailyMAPredictor(String name, Sensor<Double> sensor, int averageWindow)
    {

        return new MovingAveragePredictor(name,sensor,averageWindow) {
            @Override
            public void start(FishState model, Fisher fisher) {

                this.fisher = fisher;
                this.stoppable = model.scheduleEveryDay(this, StepOrder.INDIVIDUAL_DATA_GATHERING);

                //store your prediction:
                fisher.getDailyData().registerGatherer(name, fisher1 -> averager.getAverage(),Double.NaN);
            }
        };

    }

    /**
     * moving average predictor that calls up the sensor at the end of each trip
     * @param name name of the column we will store the prediction in
     * @param sensor function to retrieve the observation to average. Called every trip end
     * @param averageWindow length of the moving average
     * @return a concrete moving average
     */
    public static MovingAveragePredictor perTripMAPredictor(String name, Sensor<Double> sensor, int averageWindow)
    {
        final TripListener[] tripListener = new TripListener[1]; //trick to remember to stop listening when turned off
        return new MovingAveragePredictor(name,sensor,averageWindow) {
            @Override
            public void start(FishState model, Fisher fisher) {

                this.fisher = fisher;
                tripListener[0] = record -> step(model);
                fisher.addTripListener(tripListener[0]);

                //store your prediction (still every day)
                fisher.getDailyData().registerGatherer(this.name, fisher1 -> this.averager.getAverage(),Double.NaN);
            }

            @Override
            public void turnOff() {
                this.fisher.removeTripListener(tripListener[0]);
            }
        };

    }


    private MovingAveragePredictor(String name, Sensor<Double> sensor,int averageWindow) {
        this.name = name;
        this.sensor = sensor;
        averager = new MovingVariance<>(averageWindow);


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



    public double predictStandardDeviation(){
        return Math.sqrt(averager.getSmoothedObservation());
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
