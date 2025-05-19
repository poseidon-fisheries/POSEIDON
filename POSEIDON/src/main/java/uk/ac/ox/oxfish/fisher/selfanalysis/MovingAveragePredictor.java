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

package uk.ac.ox.oxfish.fisher.selfanalysis;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.MovingVariance;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

/**
 * A predictor that simply returns the moving average as a predictor for a value and a normal distribution
 * CDF for probability prediction. Instantiate through static methods rather than constructor
 * Created by carrknight on 8/18/15.
 */
public abstract class MovingAveragePredictor implements Predictor, Steppable {


    private static final long serialVersionUID = -9163159319036838257L;
    /**
     * name to use in the data column
     */
    protected final String name;
    /**
     * this we use to add to the averager
     */
    protected final Sensor<Fisher, Double> sensor;
    /**
     * this actually computes both
     */
    protected MovingVariance<Double> averager;
    protected double latestAverage = Double.NaN;
    protected Fisher fisher;

    protected Stoppable stoppable;


    private MovingAveragePredictor(final String name, final Sensor<Fisher, Double> sensor, final int averageWindow) {
        this.name = name;
        this.sensor = sensor;
        averager = new MovingVariance<>(averageWindow);


    }

    /**
     * moving average predictor that calls up the sensor every day
     *
     * @param name          name of the column we will store the prediction in
     * @param sensor        function to retrieve the daily observation
     * @param averageWindow length of the moving average
     * @return a concrete moving average
     */
    public static MovingAveragePredictor dailyMAPredictor(
        final String name,
        final Sensor<Fisher, Double> sensor,
        final int averageWindow
    ) {

        return new MovingAveragePredictor(name, sensor, averageWindow) {
            private static final long serialVersionUID = 5367766376112011498L;

            @Override
            public void start(final FishState model, final Fisher fisher) {

                this.fisher = fisher;
                this.stoppable = model.scheduleEveryDay(this, StepOrder.YEARLY_DATA_GATHERING);

                //store your prediction:
                if (name != null)
                    fisher.getDailyData()
                        .registerGatherer(name, (Gatherer<Fisher>) fisher1 -> latestAverage, Double.NaN);
            }
        };

    }

    /**
     * moving average predictor that calls up the sensor at the end of each trip
     *
     * @param name          name of the column we will store the prediction in
     * @param sensor        function to retrieve the observation to average. Called every trip end
     * @param averageWindow length of the moving average
     * @return a concrete moving average
     */
    public static MovingAveragePredictor perTripMAPredictor(
        final String name,
        final Sensor<Fisher, Double> sensor, final int averageWindow
    ) {
        final TripListener[] tripListener = new TripListener[1]; //trick to remember to stop listening when turned off
        return new MovingAveragePredictor(name, sensor, averageWindow) {
            private static final long serialVersionUID = -8807618673133476365L;

            @Override
            public void start(final FishState model, final Fisher fisher) {

                this.fisher = fisher;
                tripListener[0] = (record, fisher12) -> step(model);
                fisher.addTripListener(tripListener[0]);

                //store your prediction (still every day)
                fisher.getDailyData().registerGatherer(this.name,
                    (Gatherer<Fisher>) fisher1 -> latestAverage, Double.NaN
                );
            }

            @Override
            public void turnOff(final Fisher fisher) {
                if (tripListener[0] != null)
                    this.fisher.removeTripListener(tripListener[0]);
            }
        };

    }

    @Override
    public void step(final SimState simState) {

        final Double observation = sensor.scan(fisher);
        if (Double.isFinite(observation)) {
            averager.addObservation(observation);
            latestAverage = averager.getAverage();
        }

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
    public void turnOff(final Fisher fisher) {
        if (stoppable != null)
            stoppable.stop();
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


    public double predictStandardDeviation() {
        return Math.sqrt(averager.getSmoothedObservation());
    }

    /**
     * ask the predictor what is the probability the variable it is tracking is below a given level
     *
     * @param level the level
     * @return P(x < level)
     */
    @Override
    public double probabilityBelowThis(final double level) {
        if (averager.getSmoothedObservation() == 0)
            return level < averager.getAverage() ? 0 : 1;

        final double normalized = (level - averager.getAverage()) / Math.sqrt(averager.getSmoothedObservation());
        if (Double.isFinite(normalized))
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
    public double probabilitySumBelowThis(final double level, final int elementsInSum) {
        if (averager.getSmoothedObservation() == 0)
            return level < averager.getAverage() ? 0 : 1;

        //sum of t normally distributed values is N(t*mu,t*sigma^2)
        final double normalized = (level - elementsInSum * averager.getAverage()) / Math.sqrt(
            elementsInSum * averager.getSmoothedObservation());
        if (Double.isFinite(normalized))
            return FishStateUtilities.CNDF(normalized);
        else
            return Double.NaN;
    }


}
