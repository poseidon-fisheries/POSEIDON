/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.FadFishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FadFishingThresholdStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class FadFishingThresholdFactory implements AlgorithmFactory<FadFishingThresholdStrategy> {
    private DoubleParameter fadDeploymentsCoefficient = new FixedDoubleParameter(0.01);
    private DoubleParameter setsOnOtherFadsCoefficient = new FixedDoubleParameter(0.01);
    private DoubleParameter unassociatedSetsCoefficient = new FixedDoubleParameter(1E-8);
    private DoubleParameter fadDeploymentsProbabilityDecay = new FixedDoubleParameter(0.01);

    @SuppressWarnings("unused")
    public DoubleParameter getFadDeploymentsProbabilityDecay() {
        return fadDeploymentsProbabilityDecay;
    }


    public DoubleParameter minFadValueInMoney = new FixedDoubleParameter(100000);

    @SuppressWarnings("unused")
    public void setFadDeploymentsProbabilityDecay(DoubleParameter fadDeploymentsProbabilityDecay) {
        this.fadDeploymentsProbabilityDecay = fadDeploymentsProbabilityDecay;
    }





    @SuppressWarnings("unused")
    public DoubleParameter getUnassociatedSetsCoefficient() {
        return unassociatedSetsCoefficient;
    }

    @SuppressWarnings("unused")
    public void setUnassociatedSetsCoefficient(DoubleParameter unassociatedSetsCoefficient) {
        this.unassociatedSetsCoefficient = unassociatedSetsCoefficient;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getFadDeploymentsCoefficient() {
        return fadDeploymentsCoefficient;
    }

    @SuppressWarnings("unused")
    public void setFadDeploymentsCoefficient(DoubleParameter fadDeploymentsCoefficient) {
        this.fadDeploymentsCoefficient = fadDeploymentsCoefficient;
    }


    @SuppressWarnings("unused")
    public DoubleParameter getSetsOnOtherFadsCoefficient() {
        return setsOnOtherFadsCoefficient;
    }

    @SuppressWarnings("unused")
    public void setSetsOnOtherFadsCoefficient(DoubleParameter setsOnOtherFadsCoefficient) {
        this.setsOnOtherFadsCoefficient = setsOnOtherFadsCoefficient;
    }

    @Override
    public FadFishingThresholdStrategy apply(FishState fishState) {
        return new FadFishingThresholdStrategy(
                unassociatedSetsCoefficient.apply(fishState.random),
                fadDeploymentsCoefficient.apply(fishState.random),
                setsOnOtherFadsCoefficient.apply(fishState.random),
                fadDeploymentsProbabilityDecay.apply(fishState.random),
                minFadValueInMoney.apply(fishState.random)
        );
    }


    /**
     * Getter for property 'minFadValueInMoney'.
     *
     * @return Value for property 'minFadValueInMoney'.
     */
    public DoubleParameter getMinFadValueInMoney() {
        return minFadValueInMoney;
    }

    /**
     * Setter for property 'minFadValueInMoney'.
     *
     * @param minFadValueInMoney Value to set for property 'minFadValueInMoney'.
     */
    public void setMinFadValueInMoney(DoubleParameter minFadValueInMoney) {
        this.minFadValueInMoney = minFadValueInMoney;
    }
}
