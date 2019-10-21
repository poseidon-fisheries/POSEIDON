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

package uk.ac.ox.oxfish.geography.osmose;

import uk.ac.ox.ouce.oxfish.ExogenousMortality;
import uk.ac.ox.ouce.oxfish.cell.CellBiomass;
import uk.ac.ox.oxfish.biology.AbstractBiomassBasedBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;

/**
 * The local biology object that links up with the OSMOSE cell. In reality delegates everything to the other local osmose
 * class by assuming that recruitment age is at 0
 * Created by carrknight on 6/25/15.
 */
public class LocalOsmoseByBiomassBiology extends AbstractBiomassBasedBiology
{

    private final LocalOsmoseWithoutRecruitmentBiology delegate;



    public LocalOsmoseByBiomassBiology(
            ExogenousMortality mortality, CellBiomass counter,
            int numberOfSpecies,
            double scalingFactor, final double[] discardMortality)
    {

        delegate = new LocalOsmoseWithoutRecruitmentBiology(
                mortality,
                counter,
                scalingFactor,
                new int[numberOfSpecies],
                discardMortality

        );

    }


    /**
     * the biomass at this location for a single species.
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    @Override
    public double getBiomass(Species species) {
        return delegate.getBiomass(species);
    }

    /**
     * Tells the local biology that a fisher (or something anyway) fished this much biomass from this location
     * @param caught
     * @param notDiscarded
     * @param biology
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(
            Catch caught, Catch notDiscarded, GlobalBiology biology) {
        delegate.reactToThisAmountOfBiomassBeingFished(caught, notDiscarded,biology );
    }

    /**
     * ignored
     * @param model
     */
    @Override
    public void start(FishState model) {
        delegate.start(model);
    }

    /**
     * ignored
     */
    @Override
    public void turnOff() {
        delegate.turnOff();
    }

    public void osmoseStep() {
        delegate.osmoseStep();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
