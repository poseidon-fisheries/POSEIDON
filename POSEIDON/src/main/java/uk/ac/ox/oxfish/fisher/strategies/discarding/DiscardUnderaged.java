/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.strategies.discarding;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * Created by carrknight on 7/12/17.
 */
public class DiscardUnderaged implements DiscardingStrategy {


    /**
     * all age classes below this (but not including) will be thrown back into the sea
     */
    private final int minAge;


    public DiscardUnderaged(int minAge) {
        this.minAge = minAge;
    }


    /**
     * This strategy decides the new "catch" object, that is how much of the fish we are actually going to store
     * given how much we caught!
     *
     * @param where             where did we do the fishing
     * @param who               who did the fishing
     * @param fishCaught        the catch before any discard
     * @param hoursSpentFishing how many hours have we spent fishing
     * @param regulation        the regulation the fisher is subject to
     * @param model
     * @param random
     * @return a catch object holding how much we are actually going to load in the boat. The difference between
     * what is returned and the 'fishCaught' variable is the implicit discard
     */
    @Override
    public Catch chooseWhatToKeep(
        SeaTile where, Fisher who, Catch fishCaught, int hoursSpentFishing, Regulation regulation, FishState model,
        MersenneTwisterFast random
    ) {

        Preconditions.checkArgument(fishCaught.hasAbundanceInformation(), "this discarding equation" +
            " requires abundance information");

        //empty fish doesn't get discarded
        if (fishCaught.getTotalWeight() <= 0)
            return fishCaught;

        StructuredAbundance[] abundances = new StructuredAbundance[fishCaught.numberOfSpecies()];
        for (int species = 0; species < fishCaught.numberOfSpecies(); species++) {
            StructuredAbundance thisSpeciesAbundance = fishCaught.getAbundance(species);
            int bins = thisSpeciesAbundance.getBins();
            double[][] filtered = new double[thisSpeciesAbundance.getSubdivisions()][];
            for (int subdivision = 0; subdivision < filtered.length; subdivision++) {
                filtered[subdivision] = new double[thisSpeciesAbundance.getBins()];
                for (int bin = 0; bin < thisSpeciesAbundance.getBins(); bin++) {
                    if (bin < minAge)
                        filtered[subdivision][bin] = 0;
                    else
                        filtered[subdivision][bin] = thisSpeciesAbundance.asMatrix()[subdivision][bin];

                }
            }
            abundances[species] = new StructuredAbundance(
                filtered
            );
        }
        return new Catch(abundances, model.getBiology());


    }


    @Override
    public void start(FishState model, Fisher fisher) {

    }

    @Override
    public void turnOff(Fisher fisher) {

    }

    /**
     * Getter for property 'minAge'.
     *
     * @return Value for property 'minAge'.
     */
    public int getMinAge() {
        return minAge;
    }
}
