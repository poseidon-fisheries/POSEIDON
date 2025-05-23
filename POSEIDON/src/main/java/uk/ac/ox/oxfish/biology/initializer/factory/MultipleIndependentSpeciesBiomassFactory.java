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

package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.MultipleIndependentSpeciesBiomassInitializer;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesBiomassInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class MultipleIndependentSpeciesBiomassFactory implements
    AlgorithmFactory<MultipleIndependentSpeciesBiomassInitializer> {

    private List<AlgorithmFactory<SingleSpeciesBiomassInitializer>> factories = new LinkedList<>();

    ;
    private boolean addImaginarySpecies = true;
    private boolean constantBiomass = false;

    {
        SingleSpeciesBiomassFactory first = new SingleSpeciesBiomassFactory();
        first.setSpeciesName("Red Fish");
        factories.add(first);
        SingleSpeciesBiomassFactory second = new SingleSpeciesBiomassFactory();
        second.setSpeciesName("Blue Fish");
        factories.add(second);

    }


    public MultipleIndependentSpeciesBiomassFactory() {
    }


    public MultipleIndependentSpeciesBiomassFactory(
        List<AlgorithmFactory<SingleSpeciesBiomassInitializer>> factories,
        boolean addImaginarySpecies,
        boolean constantBiomass
    ) {
        this.factories = factories;
        this.addImaginarySpecies = addImaginarySpecies;
        this.constantBiomass = constantBiomass;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public MultipleIndependentSpeciesBiomassInitializer apply(FishState state) {


        List<SingleSpeciesBiomassInitializer> initializers = factories.stream().map(
            factory -> factory.apply(state)).collect(Collectors.toList());

        return new MultipleIndependentSpeciesBiomassInitializer(
            initializers,
            addImaginarySpecies,
            constantBiomass
        );

    }


    /**
     * Getter for property 'factories'.
     *
     * @return Value for property 'factories'.
     */
    public List<AlgorithmFactory<SingleSpeciesBiomassInitializer>> getFactories() {
        return factories;
    }

    /**
     * Setter for property 'factories'.
     *
     * @param factories Value to set for property 'factories'.
     */
    public void setFactories(
        List<AlgorithmFactory<SingleSpeciesBiomassInitializer>> factories
    ) {
        this.factories = factories;
    }


    /**
     * Getter for property 'addImaginarySpecies'.
     *
     * @return Value for property 'addImaginarySpecies'.
     */
    public boolean isAddImaginarySpecies() {
        return addImaginarySpecies;
    }

    /**
     * Setter for property 'addImaginarySpecies'.
     *
     * @param addImaginarySpecies Value to set for property 'addImaginarySpecies'.
     */
    public void setAddImaginarySpecies(boolean addImaginarySpecies) {
        this.addImaginarySpecies = addImaginarySpecies;
    }

    public boolean isConstantBiomass() {
        return constantBiomass;
    }

    public void setConstantBiomass(boolean constantBiomass) {
        this.constantBiomass = constantBiomass;
    }
}
