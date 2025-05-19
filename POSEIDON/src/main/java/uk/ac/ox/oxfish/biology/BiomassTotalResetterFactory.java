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
import uk.ac.ox.oxfish.biology.initializer.allocator.KernelizedRandomFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class BiomassTotalResetterFactory implements AlgorithmFactory<BiomassTotalResetter> {


    private String speciesName = "Species 0";

    private AlgorithmFactory<? extends BiomassAllocator> allocator = new KernelizedRandomFactory();

    private DoubleParameter yearlyBiomass = new FixedDoubleParameter(5000000);

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public BiomassTotalResetter apply(final FishState fishState) {
        return new BiomassTotalResetter(
            fishState.getBiology().getSpeciesByCaseInsensitiveName(speciesName),
            () -> allocator.apply(fishState),
            yearlyBiomass

        );
    }

    /**
     * Getter for property 'speciesName'.
     *
     * @return Value for property 'speciesName'.
     */
    public String getSpeciesName() {
        return speciesName;
    }

    /**
     * Setter for property 'speciesName'.
     *
     * @param speciesName Value to set for property 'speciesName'.
     */
    public void setSpeciesName(final String speciesName) {
        this.speciesName = speciesName;
    }

    /**
     * Getter for property 'allocator'.
     *
     * @return Value for property 'allocator'.
     */
    public AlgorithmFactory<? extends BiomassAllocator> getAllocator() {
        return allocator;
    }

    /**
     * Setter for property 'allocator'.
     *
     * @param allocator Value to set for property 'allocator'.
     */
    public void setAllocator(
        final AlgorithmFactory<? extends BiomassAllocator> allocator
    ) {
        this.allocator = allocator;
    }

    /**
     * Getter for property 'yearlyBiomass'.
     *
     * @return Value for property 'yearlyBiomass'.
     */
    public DoubleParameter getYearlyBiomass() {
        return yearlyBiomass;
    }

    /**
     * Setter for property 'yearlyBiomass'.
     *
     * @param yearlyBiomass Value to set for property 'yearlyBiomass'.
     */
    public void setYearlyBiomass(final DoubleParameter yearlyBiomass) {
        this.yearlyBiomass = yearlyBiomass;
    }
}
