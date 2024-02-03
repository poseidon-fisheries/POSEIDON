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

package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.FromLeftToRightMixedInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;


public class FromLeftToRightMixedFactory implements AlgorithmFactory<FromLeftToRightMixedInitializer> {


    /**
     * second biomass = first biomass * this value
     */
    private DoubleParameter proportionSecondSpeciesToFirst = new FixedDoubleParameter(1);

    /**
     * leftmost biomass
     */
    private DoubleParameter maximumBiomass = new FixedDoubleParameter(5000);

    /**
     * the first species' name
     */
    private String firstSpeciesName = "Species 0";

    /**
     * the second species' name
     */
    private String secondSpeciesName = "Species 1";


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FromLeftToRightMixedInitializer apply(final FishState state) {
        final FromLeftToRightMixedInitializer initializer = new FromLeftToRightMixedInitializer(
            maximumBiomass.applyAsDouble(state.getRandom()),
            proportionSecondSpeciesToFirst.applyAsDouble(state.getRandom())
        );
        initializer.setFirstSpeciesName(firstSpeciesName);
        initializer.setSecondSpeciesName(secondSpeciesName);
        return initializer;
    }

    /**
     * Getter for property 'proportionSecondSpeciesToFirst'.
     *
     * @return Value for property 'proportionSecondSpeciesToFirst'.
     */
    public DoubleParameter getProportionSecondSpeciesToFirst() {
        return proportionSecondSpeciesToFirst;
    }

    /**
     * Setter for property 'proportionSecondSpeciesToFirst'.
     *
     * @param proportionSecondSpeciesToFirst Value to set for property 'proportionSecondSpeciesToFirst'.
     */
    public void setProportionSecondSpeciesToFirst(
        final DoubleParameter proportionSecondSpeciesToFirst
    ) {
        this.proportionSecondSpeciesToFirst = proportionSecondSpeciesToFirst;
    }

    /**
     * Getter for property 'maximumBiomass'.
     *
     * @return Value for property 'maximumBiomass'.
     */
    public DoubleParameter getMaximumBiomass() {
        return maximumBiomass;
    }

    /**
     * Setter for property 'maximumBiomass'.
     *
     * @param maximumBiomass Value to set for property 'maximumBiomass'.
     */
    public void setMaximumBiomass(final DoubleParameter maximumBiomass) {
        this.maximumBiomass = maximumBiomass;
    }

    public String getFirstSpeciesName() {
        return firstSpeciesName;
    }

    public void setFirstSpeciesName(final String firstSpeciesName) {
        this.firstSpeciesName = firstSpeciesName;
    }

    public String getSecondSpeciesName() {
        return secondSpeciesName;
    }

    public void setSecondSpeciesName(final String secondSpeciesName) {
        this.secondSpeciesName = secondSpeciesName;
    }
}
