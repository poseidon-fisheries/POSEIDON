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

import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

/**
 * Local biology that just asks "schools" if they are in the area Created by carrknight on 11/17/16.
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

    public SchoolLocalBiology(
        final List<InfiniteSchool> schools,
        final SeaTile position
    ) {
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
    public double getBiomass(final Species species) {

        double sum = 0;
        for (final InfiniteSchool school : schools)
            if (school.getSpecies().equals(species) && school.contains(position))
                sum += school.getBiomassPerCell();
        return sum;
    }

    @Override
    public double getTotalBiomass() {
        return schools
            .stream()
            .filter(school -> school.contains(position))
            .mapToDouble(InfiniteSchool::getBiomassPerCell)
            .sum();
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
        final Catch caught,
        final Catch notDiscarded,
        final GlobalBiology biology
    ) {

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
