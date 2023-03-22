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
 */

package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class AbundanceFadInitializerFactory
        extends  AbstractAbundanceFadInitializerFactory {


    private DoubleParameter fadDudRate = new FixedDoubleParameter(0);


    public AbundanceFadInitializerFactory() {
    }

    public AbundanceFadInitializerFactory(
        final Supplier<SpeciesCodes> speciesCodesSupplier,
        final String... speciesNames
    ) {
        super(speciesCodesSupplier, speciesNames);
    }

    @NotNull
    protected DoubleSupplier buildCapacityGenerator(MersenneTwisterFast rng, double maximumCarryingCapacity) {
        final double probabilityOfFadBeingDud = fadDudRate.apply(rng);
        DoubleSupplier capacityGenerator;
        if(Double.isNaN(probabilityOfFadBeingDud) || probabilityOfFadBeingDud ==0)
            capacityGenerator = () -> maximumCarryingCapacity;
        else
            capacityGenerator = () -> {
                if(rng.nextFloat()<= probabilityOfFadBeingDud)
                    return 0;
                else
                    return maximumCarryingCapacity;
            };
        return capacityGenerator;
    }


    @SuppressWarnings("unused")
    public DoubleParameter getFadDudRate() {
        return fadDudRate;
    }

    public void setFadDudRate(DoubleParameter fadDudRate) {
        this.fadDudRate = fadDudRate;
    }
}
