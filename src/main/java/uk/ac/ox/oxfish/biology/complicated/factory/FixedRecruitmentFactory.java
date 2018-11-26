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

package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.*;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class FixedRecruitmentFactory implements AlgorithmFactory<RecruitmentProcess> {

    private DoubleParameter yearlyRecruits = new FixedDoubleParameter(10000);

    private boolean yearly=false;

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public RecruitmentProcess apply(FishState fishState) {

        if(yearly)
            return new YearlyRecruitmentProcess(){
                @Override
                protected double recruitYearly(Species species, Meristics meristics, StructuredAbundance abundance) {
                    return yearlyRecruits.apply(fishState.getRandom());
                }

                @Override
                public void addNoise(NoiseMaker noiseMaker) {
                    throw new RuntimeException("add noise to the doubleparameter instead");
                }
            };
        else
        return new FixedRecruitmentProcess(yearlyRecruits.apply(fishState.getRandom()));

    }

    /**
     * Getter for property 'yearlyRecruits'.
     *
     * @return Value for property 'yearlyRecruits'.
     */
    public DoubleParameter getYearlyRecruits() {
        return yearlyRecruits;
    }

    /**
     * Setter for property 'yearlyRecruits'.
     *
     * @param yearlyRecruits Value to set for property 'yearlyRecruits'.
     */
    public void setYearlyRecruits(DoubleParameter yearlyRecruits) {
        this.yearlyRecruits = yearlyRecruits;
    }

    public boolean isYearly() {
        return yearly;
    }

    public void setYearly(boolean yearly) {
        this.yearly = yearly;
    }
}
