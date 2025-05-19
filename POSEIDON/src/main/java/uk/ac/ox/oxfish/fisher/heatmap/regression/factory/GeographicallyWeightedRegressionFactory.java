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

import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicallyWeightedRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.HabitatExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.PortDistanceExtractor;
import uk.ac.ox.oxfish.geography.ManhattanDistance;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 8/22/16.
 */
public class GeographicallyWeightedRegressionFactory implements AlgorithmFactory<GeographicallyWeightedRegression> {


    private final static ObservationExtractor[] extractors = new ObservationExtractor[2];

    static {
        final ManhattanDistance distance = new ManhattanDistance();
        extractors[0] = new PortDistanceExtractor(distance, 0d);
        extractors[1] = new HabitatExtractor();

    }

    private DoubleParameter initialMin = new FixedDoubleParameter(-100);
    private DoubleParameter initialMax = new FixedDoubleParameter(100);
    private DoubleParameter initialUncertainty = new FixedDoubleParameter(10000);
    private DoubleParameter rbfBandwidth = new FixedDoubleParameter(3);
    private DoubleParameter exponentialForgetting = new FixedDoubleParameter(.98d);

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public GeographicallyWeightedRegression apply(final FishState fishState) {
        return new GeographicallyWeightedRegression(
            fishState.getMap(),
            exponentialForgetting.applyAsDouble(fishState.getRandom()),
            new ManhattanDistance(),
            rbfBandwidth.applyAsDouble(fishState.getRandom()),
            extractors,
            initialMin.applyAsDouble(fishState.getRandom()),
            initialMax.applyAsDouble(fishState.getRandom()),
            initialUncertainty.applyAsDouble(fishState.getRandom()),
            fishState.getRandom()
        );
    }


    /**
     * Getter for property 'initialMin'.
     *
     * @return Value for property 'initialMin'.
     */
    public DoubleParameter getInitialMin() {
        return initialMin;
    }

    /**
     * Setter for property 'initialMin'.
     *
     * @param initialMin Value to set for property 'initialMin'.
     */
    public void setInitialMin(final DoubleParameter initialMin) {
        this.initialMin = initialMin;
    }

    /**
     * Getter for property 'initialMax'.
     *
     * @return Value for property 'initialMax'.
     */
    public DoubleParameter getInitialMax() {
        return initialMax;
    }

    /**
     * Setter for property 'initialMax'.
     *
     * @param initialMax Value to set for property 'initialMax'.
     */
    public void setInitialMax(final DoubleParameter initialMax) {
        this.initialMax = initialMax;
    }

    /**
     * Getter for property 'initialUncertainty'.
     *
     * @return Value for property 'initialUncertainty'.
     */
    public DoubleParameter getInitialUncertainty() {
        return initialUncertainty;
    }

    /**
     * Setter for property 'initialUncertainty'.
     *
     * @param initialUncertainty Value to set for property 'initialUncertainty'.
     */
    public void setInitialUncertainty(final DoubleParameter initialUncertainty) {
        this.initialUncertainty = initialUncertainty;
    }

    /**
     * Getter for property 'rbfBandwidth'.
     *
     * @return Value for property 'rbfBandwidth'.
     */
    public DoubleParameter getRbfBandwidth() {
        return rbfBandwidth;
    }

    /**
     * Setter for property 'rbfBandwidth'.
     *
     * @param rbfBandwidth Value to set for property 'rbfBandwidth'.
     */
    public void setRbfBandwidth(final DoubleParameter rbfBandwidth) {
        this.rbfBandwidth = rbfBandwidth;
    }

    /**
     * Getter for property 'exponentialForgetting'.
     *
     * @return Value for property 'exponentialForgetting'.
     */
    public DoubleParameter getExponentialForgetting() {
        return exponentialForgetting;
    }

    /**
     * Setter for property 'exponentialForgetting'.
     *
     * @param exponentialForgetting Value to set for property 'exponentialForgetting'.
     */
    public void setExponentialForgetting(final DoubleParameter exponentialForgetting) {
        this.exponentialForgetting = exponentialForgetting;
    }
}
