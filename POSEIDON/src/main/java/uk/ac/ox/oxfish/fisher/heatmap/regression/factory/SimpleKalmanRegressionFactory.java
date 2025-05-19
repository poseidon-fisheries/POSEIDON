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

import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.SimpleKalmanRegression;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 8/3/16.
 */
public class SimpleKalmanRegressionFactory implements AlgorithmFactory<SimpleKalmanRegression> {


    private DoubleParameter distancePenalty = new FixedDoubleParameter(2);

    private DoubleParameter initialUncertainty = new FixedDoubleParameter(100);

    private DoubleParameter drift = new FixedDoubleParameter(1);


    private DoubleParameter minValue = new FixedDoubleParameter(-100);

    private DoubleParameter maxValue = new FixedDoubleParameter(100);

    private DoubleParameter optimism = new FixedDoubleParameter(0);

    private DoubleParameter evidenceUncertainty = new FixedDoubleParameter(3);

    private DoubleParameter fishingHerePenalty = new FixedDoubleParameter(0);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public SimpleKalmanRegression apply(final FishState state) {
        return new SimpleKalmanRegression(
            distancePenalty.applyAsDouble(state.getRandom()),
            drift.applyAsDouble(state.getRandom()),
            minValue.applyAsDouble(state.getRandom()),
            maxValue.applyAsDouble(state.getRandom()),
            initialUncertainty.applyAsDouble(state.getRandom()),
            evidenceUncertainty.applyAsDouble(state.getRandom()),
            optimism.applyAsDouble(state.getRandom()),
            fishingHerePenalty.applyAsDouble(state.getRandom()),
            state.getMap(),
            state.getRandom()
        );
    }


    public DoubleParameter getDistancePenalty() {
        return distancePenalty;
    }

    public void setDistancePenalty(final DoubleParameter distancePenalty) {
        this.distancePenalty = distancePenalty;
    }

    public DoubleParameter getInitialUncertainty() {
        return initialUncertainty;
    }

    public void setInitialUncertainty(final DoubleParameter initialUncertainty) {
        this.initialUncertainty = initialUncertainty;
    }

    public DoubleParameter getDrift() {
        return drift;
    }

    public void setDrift(final DoubleParameter drift) {
        this.drift = drift;
    }


    public DoubleParameter getMinValue() {
        return minValue;
    }

    public void setMinValue(final DoubleParameter minValue) {
        this.minValue = minValue;
    }

    public DoubleParameter getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(final DoubleParameter maxValue) {
        this.maxValue = maxValue;
    }

    /**
     * Getter for property 'optimism'.
     *
     * @return Value for property 'optimism'.
     */
    public DoubleParameter getOptimism() {
        return optimism;
    }

    /**
     * Setter for property 'optimism'.
     *
     * @param optimism Value to set for property 'optimism'.
     */
    public void setOptimism(final DoubleParameter optimism) {
        this.optimism = optimism;
    }

    /**
     * Getter for property 'evidenceUncertainty'.
     *
     * @return Value for property 'evidenceUncertainty'.
     */
    public DoubleParameter getEvidenceUncertainty() {
        return evidenceUncertainty;
    }

    /**
     * Setter for property 'evidenceUncertainty'.
     *
     * @param evidenceUncertainty Value to set for property 'evidenceUncertainty'.
     */
    public void setEvidenceUncertainty(final DoubleParameter evidenceUncertainty) {
        this.evidenceUncertainty = evidenceUncertainty;
    }

    /**
     * Getter for property 'fishingHerePenalty'.
     *
     * @return Value for property 'fishingHerePenalty'.
     */
    public DoubleParameter getFishingHerePenalty() {
        return fishingHerePenalty;
    }

    /**
     * Setter for property 'fishingHerePenalty'.
     *
     * @param fishingHerePenalty Value to set for property 'fishingHerePenalty'.
     */
    public void setFishingHerePenalty(final DoubleParameter fishingHerePenalty) {
        this.fishingHerePenalty = fishingHerePenalty;
    }
}
