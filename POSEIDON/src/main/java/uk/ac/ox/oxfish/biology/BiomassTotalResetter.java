/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2018-2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology;


import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

import java.util.function.Supplier;


/**
 * like biomass location resetter but instead of shuffling the current biomass levels, it forces biomass back to the level it is given
 */
public class BiomassTotalResetter extends BiomassLocationResetter {


    private static final long serialVersionUID = 7433500527073557372L;
    private final DoubleParameter yearlyBiomass;

    public BiomassTotalResetter(
        final Species species,
        final Supplier<BiomassAllocator> biomassAllocator,
        final DoubleParameter yearlyBiomass
    ) {
        super(species, biomassAllocator);
        this.yearlyBiomass = yearlyBiomass;
    }


    @Override
    protected double computeBiomassNextYear(final FishState simState) {
        return yearlyBiomass.applyAsDouble(simState.getRandom());
    }

    /**
     * Getter for property 'yearlyBiomass'.
     *
     * @return Value for property 'yearlyBiomass'.
     */
    public DoubleParameter getYearlyBiomass() {
        return yearlyBiomass;
    }


}
