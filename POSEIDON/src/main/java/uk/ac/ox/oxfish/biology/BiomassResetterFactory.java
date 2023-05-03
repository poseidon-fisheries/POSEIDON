/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology;

import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.biology.initializer.allocator.KernelizedRandomFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.function.Supplier;

public class BiomassResetterFactory implements AlgorithmFactory<BiomassLocationResetter> {



    private String speciesName = "Species 0";

    private AlgorithmFactory<? extends BiomassAllocator> algorithmFactory = new KernelizedRandomFactory();


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public BiomassLocationResetter apply(FishState fishState) {
        return new BiomassLocationResetter(
                fishState.getBiology().getSpecie(speciesName),
                () -> algorithmFactory.apply(fishState)

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
    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }

    /**
     * Getter for property 'algorithmFactory'.
     *
     * @return Value for property 'algorithmFactory'.
     */
    public AlgorithmFactory<? extends BiomassAllocator> getAlgorithmFactory() {
        return algorithmFactory;
    }

    /**
     * Setter for property 'algorithmFactory'.
     *
     * @param algorithmFactory Value to set for property 'algorithmFactory'.
     */
    public void setAlgorithmFactory(
            AlgorithmFactory<? extends BiomassAllocator> algorithmFactory) {
        this.algorithmFactory = algorithmFactory;
    }
}
