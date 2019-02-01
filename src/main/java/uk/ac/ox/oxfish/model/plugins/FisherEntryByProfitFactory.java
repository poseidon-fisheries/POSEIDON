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

package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class FisherEntryByProfitFactory implements AlgorithmFactory<FisherEntryByProfits> {


    private String profitDataColumnName = "Average Cash-Flow";

    private String costsFinalColumnName = "Average Variable Costs";

    private String populationName = FishState.DEFAULT_POPULATION_NAME;

    private DoubleParameter profitRatioToEntrantsMultiplier = new FixedDoubleParameter(100);

    private DoubleParameter maxEntrantsPerYear = new FixedDoubleParameter(50);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FisherEntryByProfits apply(FishState state) {
        return new FisherEntryByProfits(
                profitDataColumnName,
                costsFinalColumnName,
                populationName,
                profitRatioToEntrantsMultiplier.apply(state.getRandom()),
                maxEntrantsPerYear.apply(state.getRandom()).intValue()
        );
    }

    public String getProfitDataColumnName() {
        return profitDataColumnName;
    }

    public void setProfitDataColumnName(String profitDataColumnName) {
        this.profitDataColumnName = profitDataColumnName;
    }

    public String getCostsFinalColumnName() {
        return costsFinalColumnName;
    }

    public void setCostsFinalColumnName(String costsFinalColumnName) {
        this.costsFinalColumnName = costsFinalColumnName;
    }

    public String getPopulationName() {
        return populationName;
    }

    public void setPopulationName(String populationName) {
        this.populationName = populationName;
    }

    public DoubleParameter getProfitRatioToEntrantsMultiplier() {
        return profitRatioToEntrantsMultiplier;
    }

    public void setProfitRatioToEntrantsMultiplier(
            DoubleParameter profitRatioToEntrantsMultiplier) {
        this.profitRatioToEntrantsMultiplier = profitRatioToEntrantsMultiplier;
    }


    public DoubleParameter getMaxEntrantsPerYear() {
        return maxEntrantsPerYear;
    }

    public void setMaxEntrantsPerYear(DoubleParameter maxEntrantsPerYear) {
        this.maxEntrantsPerYear = maxEntrantsPerYear;
    }
}
