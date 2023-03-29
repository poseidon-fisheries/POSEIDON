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

package uk.ac.ox.oxfish.fisher.heatmap.regression.factory;

import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.ParticleFilterRegression;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 8/1/16.
 */
public class ParticleFilterRegressionFactory implements AlgorithmFactory<ParticleFilterRegression> {


    private DoubleParameter distanceNoise = new FixedDoubleParameter(.1);

    private DoubleParameter evidenceDeviation = new FixedDoubleParameter(1);

    private DoubleParameter temporalDrift = new FixedDoubleParameter(.1);

    private DoubleParameter filterSize = new FixedDoubleParameter(100);

    private DoubleParameter minValue = new FixedDoubleParameter(-100);

    private DoubleParameter maxValue = new FixedDoubleParameter(100);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ParticleFilterRegression apply(final FishState state) {
        return new ParticleFilterRegression(
            distanceNoise.applyAsDouble(state.getRandom()),
            evidenceDeviation.applyAsDouble(state.getRandom()),
            temporalDrift.applyAsDouble(state.getRandom()),
            (int) filterSize.applyAsDouble(state.getRandom()),
            state.getMap(),
            state.getRandom(),
            minValue.applyAsDouble(state.getRandom()),
            maxValue.applyAsDouble(state.getRandom())
        );
    }


    /**
     * Getter for property 'distanceNoise'.
     *
     * @return Value for property 'distanceNoise'.
     */
    public DoubleParameter getDistanceNoise() {
        return distanceNoise;
    }

    /**
     * Setter for property 'distanceNoise'.
     *
     * @param distanceNoise Value to set for property 'distanceNoise'.
     */
    public void setDistanceNoise(final DoubleParameter distanceNoise) {
        this.distanceNoise = distanceNoise;
    }

    /**
     * Getter for property 'evidenceDeviation'.
     *
     * @return Value for property 'evidenceDeviation'.
     */
    public DoubleParameter getEvidenceDeviation() {
        return evidenceDeviation;
    }

    /**
     * Setter for property 'evidenceDeviation'.
     *
     * @param evidenceDeviation Value to set for property 'evidenceDeviation'.
     */
    public void setEvidenceDeviation(final DoubleParameter evidenceDeviation) {
        this.evidenceDeviation = evidenceDeviation;
    }

    /**
     * Getter for property 'temporalDrift'.
     *
     * @return Value for property 'temporalDrift'.
     */
    public DoubleParameter getTemporalDrift() {
        return temporalDrift;
    }

    /**
     * Setter for property 'temporalDrift'.
     *
     * @param temporalDrift Value to set for property 'temporalDrift'.
     */
    public void setTemporalDrift(final DoubleParameter temporalDrift) {
        this.temporalDrift = temporalDrift;
    }

    /**
     * Getter for property 'filterSize'.
     *
     * @return Value for property 'filterSize'.
     */
    public DoubleParameter getFilterSize() {
        return filterSize;
    }

    /**
     * Setter for property 'filterSize'.
     *
     * @param filterSize Value to set for property 'filterSize'.
     */
    public void setFilterSize(final DoubleParameter filterSize) {
        this.filterSize = filterSize;
    }

    /**
     * Getter for property 'minValue'.
     *
     * @return Value for property 'minValue'.
     */
    public DoubleParameter getMinValue() {
        return minValue;
    }

    /**
     * Setter for property 'minValue'.
     *
     * @param minValue Value to set for property 'minValue'.
     */
    public void setMinValue(final DoubleParameter minValue) {
        this.minValue = minValue;
    }

    /**
     * Getter for property 'maxValue'.
     *
     * @return Value for property 'maxValue'.
     */
    public DoubleParameter getMaxValue() {
        return maxValue;
    }

    /**
     * Setter for property 'maxValue'.
     *
     * @param maxValue Value to set for property 'maxValue'.
     */
    public void setMaxValue(final DoubleParameter maxValue) {
        this.maxValue = maxValue;
    }
}
