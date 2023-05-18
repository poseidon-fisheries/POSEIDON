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

import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

/**
 * Local biology that just asks "schools" if they are in the area
 * Created by carrknight on 11/17/16.
 */
public class SchoolLocalBiology extends AbstractBiomassBasedBiology {


    /**
     * list of all the schools of fish
     */
    private final List<InfiniteSchool> schools;

    /**
     * the position of this local biology
     */
    private final SeaTile position;


    public SchoolLocalBiology(List<InfiniteSchool> schools, SeaTile position) {
        this.schools = schools;
        this.position = position;
    }

    /**
     * the biomass at this location for a single species.
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    @Override
    public double getBiomass(Species species) {

        double sum = 0;
        for (InfiniteSchool school : schools)
            if (school.getSpecies().equals(species) && school.contains(position))
                sum += school.getBiomassPerCell();
        return sum;
    }

    /**
     * ignored
     *
     * @param caught
     * @param notDiscarded
     * @param biology
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(
        Catch caught, Catch notDiscarded, GlobalBiology biology
    ) {

    }

    /**
     * ignored
     */
    @Override
    public void start(FishState model) {

    }

    /**
     * ignored
     */
    @Override
    public void turnOff() {

    }
}
