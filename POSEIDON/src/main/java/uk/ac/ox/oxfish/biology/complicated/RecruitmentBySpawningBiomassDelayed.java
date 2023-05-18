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

package uk.ac.ox.oxfish.biology.complicated;

import uk.ac.ox.oxfish.biology.Species;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Like its parent class but delays the recruitment by a number of years
 * Created by carrknight on 3/7/16.
 */
public class RecruitmentBySpawningBiomassDelayed extends RecruitmentBySpawningBiomass {

    private final Queue<Double> recruits = new LinkedList<>();
    private final int yearDelay;


    public RecruitmentBySpawningBiomassDelayed(
        final int virginRecruits,
        final double steepness,
        final double cumulativePhi,
        final boolean addRelativeFecundityToSpawningBiomass,
        final double[] maturity,
        final double[] relativeFecundity,
        final int femaleSubdivision,
        final int yearDelay
    ) {
        super(virginRecruits, steepness, cumulativePhi, addRelativeFecundityToSpawningBiomass, maturity,
            relativeFecundity,
            femaleSubdivision, false
        );
        this.yearDelay = yearDelay;
    }

    /**
     * go through all females
     *
     * @param species       the species of fish examined
     * @param meristics     the biological characteristics of the fish
     * @param abundance
     * @param dayOfTheYear
     * @param daysSimulated @return the number of male and female recruits
     */
    @Override
    public double recruit(
        final Species species,
        final Meristics meristics,
        final StructuredAbundance abundance,
        final int dayOfTheYear,
        final int daysSimulated
    ) {

        final double newRecruit = super.recruit(species, meristics, abundance, dayOfTheYear, daysSimulated);
        if (recruits.isEmpty())
            initializeQueue(newRecruit);
        assert recruits.size() == yearDelay;

        recruits.add(newRecruit);
        return recruits.poll();
    }

    public void initializeQueue(final double newRecruit) {
        while (recruits.size() < yearDelay)
            recruits.add(newRecruit);
    }
}
