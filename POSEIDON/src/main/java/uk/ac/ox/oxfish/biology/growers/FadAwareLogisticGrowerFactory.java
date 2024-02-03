/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.biology.growers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class FadAwareLogisticGrowerFactory implements AlgorithmFactory<FadAwareLogisticGrowerInitializer> {

    private DoubleParameter steepness = new FixedDoubleParameter(0.7);
    private DoubleParameter carryingCapacity = new FixedDoubleParameter(0.0);

    private boolean useLastYearBiomass = true;

    @SuppressWarnings("unused")
    public FadAwareLogisticGrowerFactory() {
    }

    @SuppressWarnings("unused")
    public boolean getUseLastYearBiomass() {
        return useLastYearBiomass;
    }

    @SuppressWarnings("unused")
    public void setUseLastYearBiomass(final boolean useLastYearBiomass) {
        this.useLastYearBiomass = useLastYearBiomass;
    }

    @Override
    public FadAwareLogisticGrowerInitializer apply(final FishState state) {
        return new FadAwareLogisticGrowerInitializer(
            carryingCapacity.applyAsDouble(state.getRandom()),
            steepness.applyAsDouble(state.getRandom()),
            useLastYearBiomass
        );
    }

    public DoubleParameter getCarryingCapacity() {
        return carryingCapacity;
    }

    public void setCarryingCapacity(final DoubleParameter carryingCapacity) {
        this.carryingCapacity = carryingCapacity;
    }

    public DoubleParameter getSteepness() {
        return steepness;
    }

    public void setSteepness(final DoubleParameter steepness) {
        this.steepness = steepness;
    }

}
