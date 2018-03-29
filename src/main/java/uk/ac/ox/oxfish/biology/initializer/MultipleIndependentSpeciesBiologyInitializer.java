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

package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.ArrayList;
import java.util.List;

public class MultipleIndependentSpeciesBiologyInitializer implements BiologyInitializer
{


    private final List<SingleSpeciesBiomassInitializer> initializers ;

    public MultipleIndependentSpeciesBiologyInitializer(
            List<SingleSpeciesBiomassInitializer> initializers) {
        this.initializers = initializers;
    }


    /**
     * if at least one species can live here, return a localBiomassBiology; else return a empty biology
     */
    @Override
    public LocalBiology generateLocal(
            GlobalBiology biology, SeaTile seaTile, MersenneTwisterFast random, int mapHeightInCells,
            int mapWidthInCells, NauticalMap map) {
        assert !initializers.isEmpty();

        LocalBiology toReturn = null;
        for(SingleSpeciesBiomassInitializer initializer : initializers) {
            LocalBiology lastgen = initializer.generateLocal(biology,
                                                             seaTile,
                                                             random,
                                                             mapHeightInCells,
                                                             mapWidthInCells,
                                                             map);
            if(toReturn == null ||
                    (toReturn instanceof EmptyLocalBiology && lastgen instanceof BiomassLocalBiology))
                toReturn = lastgen;
        }
        //return one, it doesn't matter which
        assert toReturn != null;
        return toReturn;
    }

    /**
     * after all the tiles have been instantiated this method gets called once to put anything together or to smooth
     * biomasses or whatever
     *
     * @param biology the global biology instance
     * @param map     the map which by now should have all the tiles in place
     * @param random
     * @param model   the model: it is in the process of being initialized so it should be only used to schedule
     *                stuff rather
     */
    @Override
    public void processMap(
            GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model) {

        //do not let them create their own movement!
        ArrayList<Pair<Species,BiomassMovementRule>> movements = new ArrayList<>(initializers.size());
        for (SingleSpeciesBiomassInitializer initializer : initializers)
        {
            initializer.setForceMovementOff(true);
            initializer.processMap(biology, map, random, model);
            movements.add(
                    new Pair<>(
                            biology.getSpecie(initializer.getSpeciesName()),
                            initializer.getMovementRule())
            );
        }
        //create a single movement now
        BiomassDiffuserContainer diffuser = new BiomassDiffuserContainer(
                map,random,biology,movements.toArray(new Pair[movements.size()])
        );
        model.scheduleEveryDay(diffuser, StepOrder.BIOLOGY_PHASE);


    }

    /**
     * call global generation for each, grab the species built and move on
     * @param random the random number generator
     * @param modelBeingInitialized the model we are in the process of initializing
     * @return
     */
    @Override
    public GlobalBiology generateGlobal(MersenneTwisterFast random, FishState modelBeingInitialized) {


        List<Species> species = new ArrayList<>();
        for(SingleSpeciesBiomassInitializer initializer : initializers) {

            GlobalBiology individualBiology = initializer.generateGlobal(random, modelBeingInitialized);
            assert individualBiology.getSize()==1;
            species.add(individualBiology.getSpecie(0));
        }
        return new GlobalBiology(species.toArray(new Species[species.size()]));

    }
}
