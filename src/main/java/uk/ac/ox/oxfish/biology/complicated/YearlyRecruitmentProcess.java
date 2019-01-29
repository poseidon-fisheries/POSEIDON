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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;

/**
 * recruitment that happens only once a year
 */
public abstract class YearlyRecruitmentProcess implements RecruitmentProcess {

    private static final int SPAWNING_DAY = 364;

    /**
     * Computes the number of new recruits per sex
     *
     * @param species       the species of fish examined
     * @param meristics     the biological characteristics of the fish
     * @param abundance
     * @param dayOfTheYear
     * @param daysSimulated @return the number of male + female recruits
     */
    @Override
    public double recruit(
            Species species, Meristics meristics, StructuredAbundance abundance, int dayOfTheYear, int daysSimulated) {


        //recruits yearly only
        if(daysSimulated == 1 && dayOfTheYear != SPAWNING_DAY)
            return 0d;
        else
            return recruitYearly(species, meristics, abundance);

    }

    protected abstract double recruitYearly(Species species, Meristics meristics, StructuredAbundance abundance);



}
