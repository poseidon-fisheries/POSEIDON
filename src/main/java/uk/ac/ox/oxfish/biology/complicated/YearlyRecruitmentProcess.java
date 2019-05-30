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

/**
 * recruitment function that knows how many recruits it needs every year. Can be made to act daily in which case
 * each day produces 1/365th of the recruitment
 */
public abstract class YearlyRecruitmentProcess implements RecruitmentProcess {

    private static final int SPAWNING_DAY = 364;

    private final boolean recruitEveryday;

    public YearlyRecruitmentProcess(boolean recruitEveryday) {
        this.recruitEveryday = recruitEveryday;
    }

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


        //recruits yearly only?
        if(!recruitEveryday) {
            if (daysSimulated == 1 && dayOfTheYear != SPAWNING_DAY)
            {
                return 0d;
            }
            else
            {
                return computeYearlyRecruitment(species, meristics, abundance);
            }
        }
        else
        {
            return computeYearlyRecruitment(species, meristics, abundance) * daysSimulated/365;
        }
    }

    protected abstract double computeYearlyRecruitment(Species species, Meristics meristics, StructuredAbundance abundance);



}
