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

package uk.ac.ox.oxfish.fisher.heatmap.regression.factory;

import uk.ac.ox.oxfish.fisher.heatmap.regression.bayes.GoodBadRegression;
import uk.ac.ox.oxfish.geography.ManhattanDistance;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 8/23/16.
 */
public class GoodBadRegressionFactory implements AlgorithmFactory<GoodBadRegression> {


    private DoubleParameter badAverage = new FixedDoubleParameter(-10);
    private DoubleParameter goodAverage = new FixedDoubleParameter(10);

    private DoubleParameter standardDeviation = new FixedDoubleParameter(7.5);

    private DoubleParameter distancePenalty = new FixedDoubleParameter(10);

    private DoubleParameter drift = new FixedDoubleParameter(.005);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public GoodBadRegression apply(final FishState fishState) {

        return new GoodBadRegression(
            fishState.getMap(),
            new ManhattanDistance(),
            fishState.getRandom(),
            badAverage.applyAsDouble(fishState.getRandom()),
            goodAverage.applyAsDouble(fishState.getRandom()),
            standardDeviation.applyAsDouble(fishState.getRandom()),
            distancePenalty.applyAsDouble(fishState.getRandom()),
            drift.applyAsDouble(fishState.getRandom())
        );

    }


    /**
     * Getter for property 'badAverage'.
     *
     * @return Value for property 'badAverage'.
     */
    public DoubleParameter getBadAverage() {
        return badAverage;
    }

    /**
     * Setter for property 'badAverage'.
     *
     * @param badAverage Value to set for property 'badAverage'.
     */
    public void setBadAverage(final DoubleParameter badAverage) {
        this.badAverage = badAverage;
    }

    /**
     * Getter for property 'goodAverage'.
     *
     * @return Value for property 'goodAverage'.
     */
    public DoubleParameter getGoodAverage() {
        return goodAverage;
    }

    /**
     * Setter for property 'goodAverage'.
     *
     * @param goodAverage Value to set for property 'goodAverage'.
     */
    public void setGoodAverage(final DoubleParameter goodAverage) {
        this.goodAverage = goodAverage;
    }

    /**
     * Getter for property 'standardDeviation'.
     *
     * @return Value for property 'standardDeviation'.
     */
    public DoubleParameter getStandardDeviation() {
        return standardDeviation;
    }

    /**
     * Setter for property 'standardDeviation'.
     *
     * @param standardDeviation Value to set for property 'standardDeviation'.
     */
    public void setStandardDeviation(final DoubleParameter standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    /**
     * Getter for property 'distancePenalty'.
     *
     * @return Value for property 'distancePenalty'.
     */
    public DoubleParameter getDistancePenalty() {
        return distancePenalty;
    }

    /**
     * Setter for property 'distancePenalty'.
     *
     * @param distancePenalty Value to set for property 'distancePenalty'.
     */
    public void setDistancePenalty(final DoubleParameter distancePenalty) {
        this.distancePenalty = distancePenalty;
    }

    /**
     * Getter for property 'drift'.
     *
     * @return Value for property 'drift'.
     */
    public DoubleParameter getDrift() {
        return drift;
    }

    /**
     * Setter for property 'drift'.
     *
     * @param drift Value to set for property 'drift'.
     */
    public void setDrift(final DoubleParameter drift) {
        this.drift = drift;
    }
}
