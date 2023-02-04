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

package uk.ac.ox.oxfish.model.event;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.HomogeneousGearFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.util.*;

public class AbundanceDrivenGearExogenousCatchesFactory implements
        AlgorithmFactory<AbundanceDrivenGearExogenousCatches> {


    private List<String> species = new LinkedList<>();

    private List<? extends HomogeneousGearFactory> gears = new LinkedList<>();

    private List<Number> yearlyBiomassToExtract = new LinkedList<>();


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public AbundanceDrivenGearExogenousCatches apply(FishState fishState) {

        LinkedHashMap<Species,Double> landings = new LinkedHashMap<>();
        LinkedHashMap<Species,HomogeneousAbundanceGear> gear = new LinkedHashMap<>();
        Preconditions.checkArgument(gears.size()==yearlyBiomassToExtract.size(),
                                    " mismatch between gear map and landing map for exogenous landings");
        Preconditions.checkArgument(gears.size()==species.size(),
                                    " mismatch between gear map and landing map for exogenous landings");

        for(int i=0;i<species.size(); i++)
        {
            Species species = fishState.getBiology().getSpecie(this.species.get(i));
            landings.put(
                    species,
                    yearlyBiomassToExtract.get(i).doubleValue()
            );


            gear.put(
                    species,
                    gears.get(i).apply(fishState)
            );


        }



        return new AbundanceDrivenGearExogenousCatches(landings,gear);
    }


    /**
     * Getter for property 'species'.
     *
     * @return Value for property 'species'.
     */
    public List<String> getSpecies() {
        return species;
    }

    /**
     * Setter for property 'species'.
     *
     * @param species Value to set for property 'species'.
     */
    public void setSpecies(List<String> species) {
        this.species = species;
    }

    /**
     * Getter for property 'gears'.
     *
     * @return Value for property 'gears'.
     */
    public List<? extends HomogeneousGearFactory> getGears() {
        return gears;
    }

    /**
     * Setter for property 'givenGears'.
     *
     * @param givenGears Value to set for property 'gears'.
     */
    public void setGears(List<? extends HomogeneousGearFactory> givenGears) {
        List<HomogeneousGearFactory> real = new LinkedList<>();

        FishYAML  yaml = new FishYAML();

        //force it to go through YAML
        for(int i=0;i<givenGears.size(); i++)
        {
            Object homogeneousGearFactory = givenGears.get(i);

            HomogeneousGearFactory recast = yaml.loadAs(yaml.dump(homogeneousGearFactory),
                                                        HomogeneousGearFactory.class);
            real.add(recast);

        }




        this.gears = real;
    }

    /**
     * Getter for property 'yearlyBiomassToExtract'.
     *
     * @return Value for property 'yearlyBiomassToExtract'.
     */
    public List<Number> getYearlyBiomassToExtract() {
        return yearlyBiomassToExtract;
    }

    /**
     * Setter for property 'yearlyBiomassToExtract'.
     *
     * @param yearlyBiomassToExtract Value to set for property 'yearlyBiomassToExtract'.
     */
    public void setYearlyBiomassToExtract(List<Number> yearlyBiomassToExtract) {
        this.yearlyBiomassToExtract = yearlyBiomassToExtract;
    }
}
