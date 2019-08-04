/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.boxcars;

import uk.ac.ox.oxfish.fisher.equipment.gear.components.LogisticAbundanceFilter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class FishingMortalityAgentFactory implements AlgorithmFactory<FishingMortalityAgent> {


    private  String speciesName = "Species 0";


    /**
     * the selectivity parameter A for the logistic
     */
    private DoubleParameter selectivityAParameter = new FixedDoubleParameter(23.5035);

    /**
     * the selectivity parameter B for the logistic
     */
    private DoubleParameter selectivityBParameter = new FixedDoubleParameter(9.03702);

    /**
     * whether the logistic function is log_10 or ln
     */
    private boolean selectivityInBaseTen = false;


    private boolean selectivityRounding = false;

    private boolean computeDailyFishingMortality = false;

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public FishingMortalityAgent apply(FishState fishState) {

        return new FishingMortalityAgent(
                new LogisticAbundanceFilter(
                        selectivityAParameter.apply(fishState.getRandom()),
                        selectivityBParameter.apply(fishState.getRandom()),
                        true,selectivityRounding,
                        selectivityInBaseTen



                                            ),
                fishState.getBiology().getSpecie(speciesName),

                computeDailyFishingMortality);
    }

    /**
     * Getter for property 'speciesName'.
     *
     * @return Value for property 'speciesName'.
     */
    public String getSpeciesName() {
        return speciesName;
    }

    /**
     * Setter for property 'speciesName'.
     *
     * @param speciesName Value to set for property 'speciesName'.
     */
    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }


    /**
     * Getter for property 'selectivityAParameter'.
     *
     * @return Value for property 'selectivityAParameter'.
     */
    public DoubleParameter getSelectivityAParameter() {
        return selectivityAParameter;
    }

    /**
     * Setter for property 'selectivityAParameter'.
     *
     * @param selectivityAParameter Value to set for property 'selectivityAParameter'.
     */
    public void setSelectivityAParameter(DoubleParameter selectivityAParameter) {
        this.selectivityAParameter = selectivityAParameter;
    }

    /**
     * Getter for property 'selectivityBParameter'.
     *
     * @return Value for property 'selectivityBParameter'.
     */
    public DoubleParameter getSelectivityBParameter() {
        return selectivityBParameter;
    }

    /**
     * Setter for property 'selectivityBParameter'.
     *
     * @param selectivityBParameter Value to set for property 'selectivityBParameter'.
     */
    public void setSelectivityBParameter(DoubleParameter selectivityBParameter) {
        this.selectivityBParameter = selectivityBParameter;
    }

    /**
     * Getter for property 'selectivityInBaseTen'.
     *
     * @return Value for property 'selectivityInBaseTen'.
     */
    public boolean isSelectivityInBaseTen() {
        return selectivityInBaseTen;
    }

    /**
     * Setter for property 'selectivityInBaseTen'.
     *
     * @param selectivityInBaseTen Value to set for property 'selectivityInBaseTen'.
     */
    public void setSelectivityInBaseTen(boolean selectivityInBaseTen) {
        this.selectivityInBaseTen = selectivityInBaseTen;
    }

    /**
     * Getter for property 'selectivityRounding'.
     *
     * @return Value for property 'selectivityRounding'.
     */
    public boolean isSelectivityRounding() {
        return selectivityRounding;
    }

    /**
     * Setter for property 'selectivityRounding'.
     *
     * @param selectivityRounding Value to set for property 'selectivityRounding'.
     */
    public void setSelectivityRounding(boolean selectivityRounding) {
        this.selectivityRounding = selectivityRounding;
    }

    public boolean isComputeDailyFishingMortality() {
        return computeDailyFishingMortality;
    }

    public void setComputeDailyFishingMortality(boolean computeDailyFishingMortality) {
        this.computeDailyFishingMortality = computeDailyFishingMortality;
    }
}
