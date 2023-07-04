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

package uk.ac.ox.oxfish.fisher.heatmap.regression.bayes;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.RBFDistance;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.geography.Distance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Inspired by folk interviews, what if you are only classifying spots as good/bad.
 * We assume actual draws are ~N(theta,sigma) where theta is different for good and bad spots
 * Created by carrknight on 8/22/16.
 */
public class GoodBadRegression implements GeographicalRegression<Double>, Steppable {


    private static final long serialVersionUID = 8188489285591761023L;
    /**
     * what subjective probability do we give to this spot being good
     */
    private final HashMap<SeaTile, Double> spots;
    /**
     * daily drift of probabilities towards the middle
     */
    private final double drift;
    private final Distance distance;
    private final NauticalMap map;
    /**
     * gives us the theta for the bad prior
     */
    private double badAverage;
    /**
     * gives us the theta for the good prior
     */
    private double goodAverage;
    private double standardDeviation;
    /**
     * its inverse penalizes observations that are far so that the priors are stronger
     * the penalty comes by dividing sigma by the the RBF Kernel
     */
    private final RBFDistance distancePenalty;
    private Stoppable receipt;


    public GoodBadRegression(
        final NauticalMap map,
        final Distance distance,
        final MersenneTwisterFast random,
        final double badAverage,
        final double goodAverage,
        final double deviation,
        final double distanceBandwidth,
        final double drift
    ) {
        this.map = map;
        this.drift = drift;
        this.badAverage = badAverage;
        this.goodAverage = goodAverage;
        this.standardDeviation = deviation;
        this.distance = distance;
        this.distancePenalty = new RBFDistance(distanceBandwidth);

        //each tile its own random probability
        spots = new HashMap<>();
        final List<SeaTile> tiles = map.getAllSeaTilesExcludingLandAsList();
        for (final SeaTile tile : tiles) {
            spots.put(tile, random.nextDouble());
        }

    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(final FishState model, final Fisher fisher) {
        receipt = model.scheduleEveryDay(
            this, StepOrder.DAWN);
    }


    @Override
    public void step(final SimState simState) {
        for (final Map.Entry<SeaTile, Double> probability : spots.entrySet()) {
            final double good = probability.getValue();
            Preconditions.checkArgument(good >= 0);
            Preconditions.checkArgument(good <= 1);
            final double bad = 1 - good;
            probability.setValue((good + drift) / (good + drift + bad + drift));
        }
    }

    /**
     * learn from this observation
     *
     * @param observation
     * @param fisher
     * @param model
     */
    @Override
    public void addObservation(
        final GeographicalObservation<Double> observation, final Fisher fisher, final FishState model
    ) {

        for (final Map.Entry<SeaTile, Double> probability : spots.entrySet()) {
            final double distance = this.distance.distance(
                probability.getKey(),
                observation.getTile(),
                map
            );
            final double rbf = distancePenalty.transform(distance);
            //if the evidence has even a shred of strenght, update
            if (rbf >= FishStateUtilities.EPSILON) {
                final double evidenceStrength = 1d / rbf;


                //all that follows is standard bayes


                final double goodPrior = probability.getValue();
                final double goodLikelihood = FishStateUtilities.normalPDF(
                    goodAverage, standardDeviation * evidenceStrength).apply(observation.getValue());
                final double goodPosterior = goodPrior * goodLikelihood;
                assert Double.isFinite(goodPosterior);
                assert goodPosterior >= 0;


                final double badPrior = 1d - probability.getValue();
                final double badLikelihood = FishStateUtilities.normalPDF(
                    badAverage, standardDeviation * evidenceStrength).apply(observation.getValue());
                final double badPosterior = badPrior * badLikelihood;
                assert badPosterior >= 0;
                assert Double.isFinite(badPosterior);

                if (badPosterior + goodPosterior == 0) {
                    //if it's many standard deviations away then just default to one or the other
                    if (observation.getValue() > goodAverage)
                        probability.setValue(1d);
                    else if (observation.getValue() < badAverage)
                        probability.setValue(0d);
                    else
                        probability.setValue(.5d); //if you are here that's some very poor averages/std you got
                } else
                    probability.setValue(goodPosterior / (badPosterior + goodPosterior));
            }
        }

    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff(final Fisher fisher) {
        if (receipt != null)
            receipt.stop();
    }

    /**
     * predict numerical value here
     *
     * @param tile
     * @param time
     * @param fisher
     * @param model
     * @return
     */
    @Override
    public double predict(final SeaTile tile, final double time, final Fisher fisher, final FishState model) {

        final Double probabilityGood = spots.get(tile);
        if (probabilityGood == null)
            return Double.NaN;
        else {
            return probabilityGood * goodAverage + (1 - probabilityGood) * badAverage;
        }


    }

    /**
     * turn the "V" value of the geographical observation into a number
     *
     * @param observation
     * @param fisher
     * @return
     */
    @Override
    public double extractNumericalYFromObservation(
        final GeographicalObservation<Double> observation, final Fisher fisher
    ) {
        return observation.getValue();
    }

    /**
     * Transforms the parameters used (and that can be changed) into a double[] array so that it can be inspected
     * from the outside without knowing the inner workings of the regression
     *
     * @return an array containing all the parameters of the model
     */
    @Override
    public double[] getParametersAsArray() {
        return new double[]{
            distancePenalty.getBandwidth(),
            badAverage,
            goodAverage,
            standardDeviation
        };
    }

    /**
     * given an array of parameters (of size equal to what you'd get if you called the getter) the regression is supposed
     * to transition to these parameters
     *
     * @param parameterArray the new parameters for this regresssion
     */
    @Override
    public void setParameters(final double[] parameterArray) {

        assert parameterArray.length == 4;
        distancePenalty.setBandwidth(parameterArray[0]);
        badAverage = parameterArray[1];
        goodAverage = parameterArray[2];
        standardDeviation = parameterArray[3];
    }
}
