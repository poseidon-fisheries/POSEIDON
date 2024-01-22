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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Just a biology that has 0 biomass of everything Created by carrknight on 4/11/15.
 */
public class EmptyLocalBiology implements LocalBiology {

    /**
     * the biomass is 0 for everything
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    @Override
    public double getBiomass(final Species species) {
        return 0d;
    }

    @Override
    public double getTotalBiomass() {
        return 0;
    }

    /**
     * Tells the local biology that a fisher (or something anyway) fished this much biomass from this location
     *
     * @param caught
     * @param notDiscarded
     * @param biology
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(
        final Catch caught,
        final Catch notDiscarded,
        final GlobalBiology biology
    ) {
        Preconditions.checkArgument(
            caught.getTotalWeight() == 0,
            "It's impossible to take biomass from the empty biology"
        );
    }

    @Override
    public StructuredAbundance getAbundance(final Species species) {
        return new StructuredAbundance(new double[0]);
    }

    /**
     * ignored
     */
    @Override
    public void start(final FishState model) {

    }

    /**
     * ignored
     */
    @Override
    public void turnOff() {

    }
}
