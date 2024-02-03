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

import uk.ac.ox.oxfish.fisher.equipment.gear.components.LogisticSimpleFilter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class FishingMortalityAgentFactory implements AlgorithmFactory<FishingMortalityAgent> {


    private String speciesName = "Species 0";


    /**
     * the selectivity parameter A for the logistic (simple version)
     */
    private DoubleParameter selexParameter1 = new FixedDoubleParameter(23.5035);

    /**
     * the selectivity parameter B for the logistic
     */
    private DoubleParameter selexParameter2 = new FixedDoubleParameter(9.03702);


    private boolean computeDailyFishingMortality = false;

    private boolean selectivityRounding = false;

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public FishingMortalityAgent apply(final FishState fishState) {

        return new FishingMortalityAgent(
            new LogisticSimpleFilter(
                true, selectivityRounding,
                selexParameter1.applyAsDouble(fishState.getRandom()),
                selexParameter2.applyAsDouble(fishState.getRandom())


            ),
            fishState.getBiology().getSpeciesByCaseInsensitiveName(speciesName),

            computeDailyFishingMortality
        );
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
    public void setSpeciesName(final String speciesName) {
        this.speciesName = speciesName;
    }


    /**
     * Getter for property 'selectivityAParameter'.
     *
     * @return Value for property 'selectivityAParameter'.
     */
    public DoubleParameter getSelexParameter1() {
        return selexParameter1;
    }

    /**
     * Setter for property 'selectivityAParameter'.
     *
     * @param selexParameter1 Value to set for property 'selectivityAParameter'.
     */
    public void setSelexParameter1(final DoubleParameter selexParameter1) {
        this.selexParameter1 = selexParameter1;
    }

    /**
     * Getter for property 'selectivityBParameter'.
     *
     * @return Value for property 'selectivityBParameter'.
     */
    public DoubleParameter getSelexParameter2() {
        return selexParameter2;
    }

    /**
     * Setter for property 'selectivityBParameter'.
     *
     * @param selexParameter2 Value to set for property 'selectivityBParameter'.
     */
    public void setSelexParameter2(final DoubleParameter selexParameter2) {
        this.selexParameter2 = selexParameter2;
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
    public void setSelectivityRounding(final boolean selectivityRounding) {
        this.selectivityRounding = selectivityRounding;
    }

    public boolean isComputeDailyFishingMortality() {
        return computeDailyFishingMortality;
    }

    public void setComputeDailyFishingMortality(final boolean computeDailyFishingMortality) {
        this.computeDailyFishingMortality = computeDailyFishingMortality;
    }
}
