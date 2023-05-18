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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.RBFDistance;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.Arrays;

/**
 * A recursive kernel predictor. Because it needs to predict always in the same spot time will be a forgetting factor
 * instead of the more appropriate additional data dimension.
 * It uses product (gaussian) Kernel
 * Created by carrknight on 7/8/16.
 */
public class KernelTilePredictor {


    private final double forgettingFactor;
    private final SeaTile whereAmIPredicting;
    /**
     * the bandwidth are within the distance objects
     */
    private final ObservationExtractor[] extractors;
    private final RBFDistance kerneler = new RBFDistance(0); //this bandwidth gets changed at each step
    private double currentPrediction = 0;
    private double currentDenominator = 0;
    private double[] bandwidths;

    public KernelTilePredictor(
        double forgettingFactor,
        SeaTile whereAmIPredicting,
        Pair<ObservationExtractor, Double>... extractorsAndBandwidths
    ) {
        this.forgettingFactor = forgettingFactor;
        this.whereAmIPredicting = whereAmIPredicting;
        assert extractorsAndBandwidths.length > 0;
        extractors = new ObservationExtractor[extractorsAndBandwidths.length];
        bandwidths = new double[extractorsAndBandwidths.length];
        for (int i = 0; i < extractorsAndBandwidths.length; i++) {
            extractors[i] = extractorsAndBandwidths[i].getFirst();
            bandwidths[i] = extractorsAndBandwidths[i].getSecond();
        }
    }

    public void addObservation(GeographicalObservation<Double> observation, Fisher fisher, FishState model) {
        //compute kernel
        double kernel = 1;
        for (int i = 0; i < extractors.length; i++) {
            kerneler.setBandwidth(bandwidths[i]);
            kernel *= kerneler.distance(
                extractors[i].extract(observation.getTile(), observation.getTime(), fisher, model),
                extractors[i].extract(whereAmIPredicting, observation.getTime(), fisher, model)

            );
        }

        //update denominator
        currentDenominator = currentDenominator * forgettingFactor + kernel;
        Preconditions.checkArgument(Double.isFinite(currentDenominator), currentDenominator + " , " +
            forgettingFactor + " , " + kernel + " , " + Arrays.toString(bandwidths));
        //update predictor
        if (currentDenominator > 0)
            currentPrediction += (observation.getValue() - currentPrediction) * kernel / currentDenominator;

    }


    public double getCurrentPrediction() {
        return currentPrediction;
    }

    public double getCurrentDenominator() {
        return currentDenominator;
    }

    public double getForgettingFactor() {
        return forgettingFactor;
    }

    public SeaTile getWhereAmIPredicting() {
        return whereAmIPredicting;
    }

    /**
     * Getter for property 'bandwidths'.
     *
     * @return Value for property 'bandwidths'.
     */
    public double[] getBandwidths() {
        return bandwidths;
    }

    /**
     * Setter for property 'bandwidths'.
     *
     * @param bandwidths Value to set for property 'bandwidths'.
     */
    public void setBandwidths(double[] bandwidths) {
        this.bandwidths = bandwidths;
    }
}
