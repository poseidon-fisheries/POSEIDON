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

import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.SimpleKalmanRegression;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 8/3/16.
 */
public class SimpleKalmanRegressionFactory implements AlgorithmFactory<SimpleKalmanRegression>{


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
    public SimpleKalmanRegression apply(FishState state) {
        return new SimpleKalmanRegression(
                distancePenalty.apply(state.getRandom()),
                drift.apply(state.getRandom()),
                minValue.apply(state.getRandom()),
                maxValue.apply(state.getRandom()),
                initialUncertainty.apply(state.getRandom()),
                evidenceUncertainty.apply(state.getRandom()),
                optimism.apply(state.getRandom()),
                fishingHerePenalty.apply(state.getRandom()),
                state.getMap(),
                state.getRandom()) ;
    }


    public DoubleParameter getDistancePenalty() {
        return distancePenalty;
    }

    public void setDistancePenalty(DoubleParameter distancePenalty) {
        this.distancePenalty = distancePenalty;
    }

    public DoubleParameter getInitialUncertainty() {
        return initialUncertainty;
    }

    public void setInitialUncertainty(DoubleParameter initialUncertainty) {
        this.initialUncertainty = initialUncertainty;
    }

    public DoubleParameter getDrift() {
        return drift;
    }

    public void setDrift(DoubleParameter drift) {
        this.drift = drift;
    }


    public DoubleParameter getMinValue() {
        return minValue;
    }

    public void setMinValue(DoubleParameter minValue) {
        this.minValue = minValue;
    }

    public DoubleParameter getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(DoubleParameter maxValue) {
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
    public void setOptimism(DoubleParameter optimism) {
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
    public void setEvidenceUncertainty(DoubleParameter evidenceUncertainty) {
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
    public void setFishingHerePenalty(DoubleParameter fishingHerePenalty) {
        this.fishingHerePenalty = fishingHerePenalty;
    }
}
