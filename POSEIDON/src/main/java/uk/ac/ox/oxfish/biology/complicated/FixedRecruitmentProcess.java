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

package uk.ac.ox.oxfish.biology.complicated;

import uk.ac.ox.oxfish.biology.Species;

public class FixedRecruitmentProcess implements RecruitmentProcess {


    private final double yearlyRecruits;

    private NoiseMaker noiseMaker = new NoNoiseMaker();


    public FixedRecruitmentProcess(double yearlyRecruits) {
        this.yearlyRecruits = yearlyRecruits;
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
        Species species, Meristics meristics, StructuredAbundance abundance, int dayOfTheYear, int daysSimulated
    ) {
        return (yearlyRecruits * (1 + noiseMaker.get())) * ((double) daysSimulated) / 365d;
    }

    /**
     * give a function to generate noise as % of recruits this year
     *
     * @param noiseMaker the function that generates percentage changes. 1 means no noise.
     */
    @Override
    public void addNoise(NoiseMaker noiseMaker) {
        this.noiseMaker = noiseMaker;
    }
}
