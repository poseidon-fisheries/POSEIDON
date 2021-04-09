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

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;

public class FadAwareLogisticGrowerInitializer implements LogisticGrowerInitializer {

    private double malthusianParameter;
    private double distributionalWeight;
    private boolean useLastYearBiomass;

    public FadAwareLogisticGrowerInitializer(
        final double malthusianParameter,
        final double distributionalWeight,
        final boolean useLastYearBiomass
    ) {
        this.malthusianParameter = malthusianParameter;
        this.distributionalWeight = distributionalWeight;
        this.useLastYearBiomass = useLastYearBiomass;
    }

    @SuppressWarnings("unused")
    public double getMalthusianParameter() { return malthusianParameter; }

    @SuppressWarnings("unused")
    public void setMalthusianParameter(final double malthusianParameter) {
        this.malthusianParameter = malthusianParameter;
    }

    public double getDistributionalWeight() { return distributionalWeight; }

    public void setDistributionalWeight(final double distributionalWeight) {
        this.distributionalWeight = distributionalWeight;
    }

    @SuppressWarnings("unused")
    public boolean isUseLastYearBiomass() { return useLastYearBiomass; }

    @SuppressWarnings("unused")
    public void setUseLastYearBiomass(final boolean useLastYearBiomass) {
        this.useLastYearBiomass = useLastYearBiomass;
    }

    @Override
    public void initializeGrower(
        final Map<SeaTile, BiomassLocalBiology> tiles,
        final FishState state,
        final MersenneTwisterFast random,
        final Species species
    ) {
        state.registerStartable(new FadAwareLogisticGrower(
            species,
            malthusianParameter,
            distributionalWeight,
            useLastYearBiomass,
            tiles.values()
        ));
    }

}
