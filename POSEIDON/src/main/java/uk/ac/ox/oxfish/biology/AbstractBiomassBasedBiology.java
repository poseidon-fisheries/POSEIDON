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

package uk.ac.ox.oxfish.biology;

import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;

import java.util.logging.Logger;

/**
 * An abstract local biology class that marks the children as being based on Biomass rather than abudance.
 * When actual abundance is required (and this is already a suspicious call) just assume every fish is of age 0
 * and just return an array with [biomass/weightAtAge0, 0, 0, ... , 0 ]
 * Created by carrknight on 3/3/16.
 */
public abstract class AbstractBiomassBasedBiology implements LocalBiology {

    boolean warned = false;

    @Override
    public StructuredAbundance getAbundance(final Species species) {

        return new StructuredAbundance(turnBiomassIntoFakeNumberArray(
            getBiomass(species),
            species
        ));
    }

    /**
     * given that there is this much biomass, how many fish are there if they are all age 0? Return it as an array.
     * (if the weight at age 0 is 0 then return 1 as the number of fish)
     *
     * @param biomass total biomass available
     * @param species link to fish biomass
     * @return an array of fish where all the fish are age 0 and their number is biomass/weight rounded down
     */
    private double[] turnBiomassIntoFakeNumberArray(final double biomass, final Species species) {
        warnIfNeeded();

        final double[] toReturn = new double[species.getNumberOfBins()];
        if (biomass == 0)
            return toReturn;
        final double weight = species.getWeight(0, 0);
        if (weight > 0)
            toReturn[0] = (biomass / weight);
        else
            toReturn[0] = 1;
        return toReturn;

    }

    public void warnIfNeeded() {
        if (!warned)
            Logger.getGlobal()
                .warning(
                    "Calling a number based biology method on a biomass based local biology. This is usually not desired");
    }

}
