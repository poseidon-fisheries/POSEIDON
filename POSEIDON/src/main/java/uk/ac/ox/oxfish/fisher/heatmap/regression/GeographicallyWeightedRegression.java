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

package uk.ac.ox.oxfish.fisher.heatmap.regression;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.RBFDistance;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.InterceptExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.LeastSquareFilter;
import uk.ac.ox.oxfish.geography.Distance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Each tile is a separate local linear regression where the weight of the observation is given by RBF distance
 * Created by carrknight on 8/18/16.
 */
public class GeographicallyWeightedRegression implements GeographicalRegression<Double> {


    /**
     * functions used to turn an observation into a double array of features
     */
    private final ObservationExtractor[] extractors;


    private final HashMap<SeaTile, LeastSquareFilter> lowesses = new HashMap<>();


    private final Distance distance;


    private final RBFDistance kernel;
    private final NauticalMap map;


    public GeographicallyWeightedRegression(
        NauticalMap map, double exponentialForgetting,
        Distance distance, double rbfBandwidth,
        ObservationExtractor[] nonInterceptExtractors,
        double initialMin,
        double initialMax,
        double initialUncertainty,
        MersenneTwisterFast random
    ) {
        this.distance = distance;
        this.map = map;
        Preconditions.checkArgument(initialMax > initialMin);
        //get extractors and add intercept
        this.extractors = new ObservationExtractor[nonInterceptExtractors.length + 1];
        for (int i = 0; i < nonInterceptExtractors.length; i++)
            this.extractors[i + 1] = nonInterceptExtractors[i];
        this.extractors[0] = new InterceptExtractor();

        this.kernel = new RBFDistance(rbfBandwidth);

        //each tile its own lowess with a random intercept
        List<SeaTile> tiles = map.getAllSeaTilesExcludingLandAsList();
        for (SeaTile tile : tiles) {
            double[] beta = new double[nonInterceptExtractors.length + 1];
            beta[0] = random.nextDouble() * (initialMax - initialMin) + initialMin;
            lowesses.put(tile, new LeastSquareFilter(nonInterceptExtractors.length + 1,
                initialUncertainty, beta,
                exponentialForgetting
            ));
        }


    }

    @Override
    public void addObservation(
        GeographicalObservation<Double> observation, Fisher fisher, FishState model
    ) {
        //add observation with 1/weight as sigma^2
        double[] features = ObservationExtractor.convertToFeatures(
            observation.getTile(), observation.getTime(),
            fisher, extractors, model
        );
        //go through all the tiles
        for (Map.Entry<SeaTile, LeastSquareFilter> lowess : lowesses.entrySet()) {
            double sigma = 1d /
                kernel.transform(distance.distance(lowess.getKey(), observation.getTile(), map));

            if (!Double.isFinite(sigma)) {
                lowess.getValue().increaseUncertainty();
                continue;
            } else {
                lowess.getValue().addObservation(features, observation.getValue(), sigma);
            }
        }
    }


    /**
     * returns the current kernel prediction
     *
     * @return
     */
    @Override
    public double predict(SeaTile tile, double time, Fisher fisher, FishState model) {

        LeastSquareFilter predictor = lowesses.get(tile);
        if (predictor == null)
            return Double.NaN;
        else {
            double[] features = ObservationExtractor.convertToFeatures(
                tile, time, fisher, extractors, model);
            double prediction = 0;
            for (int i = 0; i < features.length; i++)
                prediction += features[i] * predictor.getBeta()[i];
            return prediction;
        }

    }


    @VisibleForTesting
    public double[] getBeta(SeaTile tile) {
        LeastSquareFilter predictor = lowesses.get(tile);
        if (predictor == null)
            return null;
        else
            return predictor.getBeta();
    }

    /**
     * ignored
     */
    @Override
    public void start(FishState model, Fisher fisher) {

    }

    /**
     * ignored
     */
    @Override
    public void turnOff(Fisher fisher) {

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
     * The only hyper-parameter really is the forgetting value
     */
    @Override
    public double[] getParametersAsArray() {

        double currentForgetting = lowesses.values().iterator().next().getExponentialForgetting();
        //check that they all have the same forgetting!
        assert lowesses.values().stream().allMatch(
            lowessTile -> lowessTile.getExponentialForgetting() == currentForgetting);
        return new double[]{currentForgetting, kernel.getBandwidth()};

    }

    /**
     * receives and modifies the forgetting value
     */
    @Override
    public void setParameters(double[] parameterArray) {

        assert parameterArray.length == 2;
        lowesses.values().forEach(lowessTile -> lowessTile.setExponentialForgetting(parameterArray[0]));
        kernel.setBandwidth(parameterArray[1]);

    }
}
