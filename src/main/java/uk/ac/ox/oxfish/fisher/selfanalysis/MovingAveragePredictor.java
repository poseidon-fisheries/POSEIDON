package uk.ac.ox.oxfish.fisher.selfanalysis;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.MovingVariance;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

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
    protected MovingVariance<Double> averager;

    protected double latestAverage = Double.NaN;

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
                this.stoppable = model.scheduleEveryDay(this, StepOrder.YEARLY_DATA_GATHERING);

                //store your prediction:
                if(name!=null)
                    fisher.getDailyData().registerGatherer(name, fisher1 -> this.latestAverage,Double.NaN);
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
                fisher.getDailyData().registerGatherer(this.name, fisher1 -> this.latestAverage,Double.NaN);
            }

            @Override
            public void turnOff() {
                if(tripListener[0]!=null)
                    this.fisher.removeTripListener(tripListener[0]);
            }
        };

    }


    private MovingAveragePredictor(String name, Sensor<Double> sensor,int averageWindow) {
        this.name = name;
        this.sensor = sensor;
        averager = new MovingVariance<>(averageWindow);


    }

    /**
     * this is called if something happens (gear change for example) that makes us think the old predictors are full of garbage
     * data and need to be reset
     */
    @Override
    public void reset() {
        averager = new MovingVariance<>(averager.getSize());
        assert !averager.isReady();
    }

    @Override
    public void turnOff() {
        if(stoppable!= null)
            stoppable.stop();
    }


    @Override
    public void step(SimState simState)
    {

        Double observation = sensor.scan(fisher);
        if(Double.isFinite(observation)) {
            averager.addObservation(observation);
            latestAverage = averager.getAverage();
        }

    }

    /**
     * ask the predictor the expected value of the variable it is tracking
     *
     * @return the expected value
     */
    @Override
    public double predict() {
        return latestAverage;
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


    /**
     * Asks the predictor what is the probability that a sum of #elementsInSum of identically distributed elements of
     * this predictor is below the given level
     *
     * @param level         the level the sum has to be below of
     * @param elementsInSum the number of i.i.d independent variables given by the predictor summed together
     * @return a probability value
     */
    @Override
    public double probabilitySumBelowThis(double level, int elementsInSum)
    {
        if(averager.getSmoothedObservation()==0)
            return level < averager.getAverage() ? 0 : 1;

        //sum of t normally distributed values is N(t*mu,t*sigma^2)
        double normalized = (level-elementsInSum * averager.getAverage()) / Math.sqrt(
                elementsInSum * averager.getSmoothedObservation());
        if(Double.isFinite(normalized))
            return FishStateUtilities.CNDF(normalized);
        else
            return Double.NaN;
    }


}
