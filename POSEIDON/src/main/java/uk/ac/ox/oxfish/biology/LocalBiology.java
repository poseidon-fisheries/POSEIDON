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

package uk.ac.ox.oxfish.biology;

import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.Startable;

/**
 * This is supposed to be a container of biological information we can attach to one or multiple sea tiles (because there
 * will probably be issues with data not having the same resolution as our model). For now it holds just local biomass
 * information
 * Created by carrknight on 4/11/15.
 */
public interface LocalBiology extends Startable {


    /**
     * The biomass at this location for the specified species.
     *
     * @param species an iterable of the species we are interested in.
     * @return the sum of biomass for the specified species.
     */
    default double getTotalBiomass(final Iterable<? extends Species> species) {
        double totalBiomass = 0;
        for (Species currentSpecies : species) {
            totalBiomass += getBiomass(currentSpecies);
        }


        return totalBiomass;
    }

    /**
     * the biomass at this location for a single species.
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    double getBiomass(Species species);

    /**
     * Tells the local biology that a fisher (or something anyway) fished this much biomass from this location
     *
     * @param caught       the amount fished out of the sea
     * @param notDiscarded the amount retained in the boat (not thrown out)
     * @param biology
     */
    void reactToThisAmountOfBiomassBeingFished(
        Catch caught, Catch notDiscarded, GlobalBiology biology
    );


    StructuredAbundance getAbundance(Species species);


}
