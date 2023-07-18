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

package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import ec.util.MersenneTwisterFast;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.bayes.Belief;
import uk.ac.ox.oxfish.fisher.heatmap.regression.bayes.ParticleFilter;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A geographical regression which at its core is just a long hash map of separate
 * <p>
 * Created by carrknight on 8/1/16.
 */
public class ParticleFilterRegression implements GeographicalRegression<Double> {


    private final HashMap<SeaTile, ParticleFilter<Double>> filters = new HashMap<>();
    private final int filterSizes;
    private final NauticalMap map;
    private final MersenneTwisterFast random;
    /**
     * the % increase per unit of distance in evidence variance
     */
    private double distanceNoise;
    /**
     * the standard deviation  of p(e|X)~N(observation,.)
     */
    private double evidenceDeviation;
    /**
     * standard deviation of the daily shock to each particle applied
     */
    private double temporalDrift;
    private double minValue;

    private double maxValue;
    private Stoppable receipt;


    public ParticleFilterRegression(
        double distanceNoise, double evidenceDeviation, double temporalDrift, int filterSizes,
        NauticalMap map, MersenneTwisterFast random, double minValue, double maxValue
    ) {
        this.distanceNoise = distanceNoise;
        this.evidenceDeviation = evidenceDeviation;
        this.temporalDrift = temporalDrift;
        this.filterSizes = filterSizes;
        this.map = map;
        this.random = random;
        this.minValue = minValue;
        this.maxValue = maxValue;

        List<SeaTile> tiles = map.getAllSeaTilesExcludingLandAsList();
        for (SeaTile tile : tiles)
            filters.put(tile, ParticleFilter.defaultParticleFilter(
                minValue,
                maxValue,
                temporalDrift,
                filterSizes,
                random
            ));
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model, Fisher fisher) {

        //every morning drift out a bit
        receipt = model.scheduleEveryDay((Steppable) simState -> {
            for (ParticleFilter<Double> filter : filters.values())
                filter.drift(random);
        }, StepOrder.DAWN);

    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff(Fisher fisher) {
        if (receipt != null)
            receipt.stop();
    }

    @Override
    public double predict(
        SeaTile tile, double time, Fisher fisher, FishState model
    ) {
        return getMean(tile);
    }

    public double getMean(SeaTile tile) {
        ParticleFilter<Double> filter = filters.get(tile);
        if (filter == null)
            return Double.NaN;
        else {
            Belief<Double> belief = filter.getBelief();
            if (belief.getTotalWeight() <= 0)
                return Double.NaN;
            double mean = 0;
            for (Map.Entry<Double, Double> temp : belief.getCdf().entrySet()) {
                mean += temp.getKey() * temp.getValue();
            }
            return mean;
        }


    }

    @Override
    public void addObservation(GeographicalObservation<Double> observation, Fisher fisher, FishState model) {

        for (Map.Entry<SeaTile, ParticleFilter<Double>> filter : filters.entrySet()) {
            double distance = map.distance(observation.getTile(), filter.getKey());
            double totalDeviation = evidenceDeviation * (1 + distanceNoise * distance);
            Function<Double, Double> evidenceProbability = FishStateUtilities.normalPDF(
                observation.getValue(), totalDeviation
            );
            //if there is a meaningful difference between probability max and min then there is some value in this
            if (totalDeviation < (maxValue - minValue) / 2)
                filter.getValue().updateGivenEvidence(
                    evidenceProbability, random);

        }

    }

    public double getStandardDeviation(SeaTile tile) {
        ParticleFilter<Double> filter = filters.get(tile);
        return Belief.getSummaryStatistics(filter.getBelief())[1];


    }

    public double getDistanceNoise() {
        return distanceNoise;
    }

    public double getEvidenceDeviation() {
        return evidenceDeviation;
    }

    public double getTemporalDrift() {
        return temporalDrift;
    }

    public int getFilterSizes() {
        return filterSizes;
    }

    public NauticalMap getMap() {
        return map;
    }

    public MersenneTwisterFast getRandom() {
        return random;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    /**
     * It's already a double so return it!
     */
    @Override
    public double extractNumericalYFromObservation(
        GeographicalObservation<Double> observation, Fisher fisher
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

        return new double[]{distanceNoise, evidenceDeviation};

    }

    /**
     * given an array of parameters (of size equal to what you'd get if you called the getter) the regression is supposed
     * to transition to these parameters
     *
     * @param parameterArray the new parameters for this regresssion
     */
    @Override
    public void setParameters(double[] parameterArray) {
        assert parameterArray.length == 2;
        distanceNoise = parameterArray[0];
        evidenceDeviation = parameterArray[1];

    }
}
