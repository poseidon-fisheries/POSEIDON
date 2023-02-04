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

package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;

/**
 * Basically an abstract class that creates GlobalBiology objects from a list of names; useful for all the simple
 * biologies where species are just names and nothing else
 * Created by carrknight on 11/5/15.
 */
public  abstract class AbstractBiologyInitializer implements BiologyInitializer {

    /**
     * creates the global biology object for the model
     *
     * @param random                the random number generator
     * @param modelBeingInitialized the model we are in the process of initializing
     * @return a global biology object
     */
    @Override
    public GlobalBiology generateGlobal(
            MersenneTwisterFast random,
            FishState modelBeingInitialized) {
        //turn list of names into list of species
        String[] names = getSpeciesNames();
        Species[] speciesArray = new Species[names.length];
        for(int i=0; i< names.length; i++)
        {
            speciesArray[i] = new Species(names[i]);
        }












        //initialize all the data collection stuff
        for (Species species : speciesArray) {
            final String columnName = species + " Recruitment";
            modelBeingInitialized.getYearlyCounter().addColumn(
                    columnName);
            modelBeingInitialized.getYearlyDataSet().registerGatherer(
                    columnName,
                    new Gatherer<FishState>() {
                        @Override
                        public Double apply(
                                FishState state) {
                            return modelBeingInitialized.getYearlyCounter().getColumn(
                                    columnName);
                        }
                    }, 0d);
        }

        return new GlobalBiology(speciesArray);
    }

    /**
     * Get a list of the species with their names. The size of this array determines the size of the model array
     *
     * @return
     */
     abstract public String[] getSpeciesNames();
}
