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
 * A process to decide how many new fish of each sex are generated this year
 * Created by carrknight on 3/1/16.
 */
public interface RecruitmentProcess
{


    /**
     * Computes the number of new recruits per sex
     * @param species the species of fish examined
     * @param meristics the biological characteristics of the fish
     * @param abundance
     * @return the number of male + female recruits
     */
    double recruit(
            Species species,
            Meristics meristics, StructuredAbundance abundance);


    /**
     * give a function to generate noise as % of recruits this year
     * @param noiseMaker the function that generates percentage changes. 1 means no noise.
     */
    void addNoise(NoiseMaker noiseMaker);



}
