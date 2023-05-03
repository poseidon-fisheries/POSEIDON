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
import uk.ac.ox.oxfish.biology.growers.IndependentLogisticBiomassGrower;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Logistic recruitment: weight all the biomass, take a logisitc step and transform it back into abundance
 * Created by carrknight on 7/5/17.
 */
public class LogisticRecruitmentProcess extends YearlyRecruitmentProcess {


    final private double carryingCapacity;

    final private double malthusianParameter;

    private NoiseMaker noise = new NoNoiseMaker();


    public LogisticRecruitmentProcess(double carryingCapacity,
                                      double malthusianParameter,
                                      boolean recruitEveryDay) {
        super(recruitEveryDay);
        this.carryingCapacity = carryingCapacity;
        this.malthusianParameter = malthusianParameter;
    }

    /**
     * Computes the number of new recruits per sex
     *
     * @param species      the species of fish examined
     * @param meristics    the biological characteristics of the fish
     * @param abundance
     * @return the number of male + female recruits
     */
    @Override
    public double computeYearlyRecruitment(
            Species species, Meristics meristics, StructuredAbundance abundance) {

        //weigh
        double biomass = FishStateUtilities.weigh(abundance,meristics);

        double nextBiomass = IndependentLogisticBiomassGrower.logisticStep(
                biomass + noise.get(),carryingCapacity,malthusianParameter
        );

        double recruitmentBiomass = nextBiomass-biomass;
        double recruitAverageWeight = 0;
        for(int i=0; i<abundance.getSubdivisions(); i++)
            recruitAverageWeight += meristics.getWeight(i,0);
        recruitAverageWeight/= (double) meristics.getNumberOfSubdivisions();

        assert  recruitmentBiomass >=0;

        //turn weight into # of recruits and return it!
        return recruitmentBiomass > 0 ? (recruitmentBiomass/recruitAverageWeight) :0;
    }

    /**
     * give a function to generate noise as % of recruits this year
     *
     * @param noiseMaker the function that generates percentage changes. 1 means no noise.
     */
    @Override
    public void addNoise(NoiseMaker noiseMaker) {
            this.noise = noiseMaker;
    }
}
