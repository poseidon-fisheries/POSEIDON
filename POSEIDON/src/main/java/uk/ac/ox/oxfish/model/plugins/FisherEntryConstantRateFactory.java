/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2019-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class FisherEntryConstantRateFactory implements AlgorithmFactory<FisherEntryConstantRate> {


    /**
     * we expect activeBoats * (growthRateInPercentage) to be the new entrants next year
     */
    private DoubleParameter growthRateInPercentage = new FixedDoubleParameter(0.029);

    /**
     * which population of boats are we growing; this has to be both the name of the fishery factory and a tag so that we know
     * which boats belong to it
     */
    private String populationName = "population0";


    private DoubleParameter firstYearEntryOccurs = new FixedDoubleParameter(-1);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FisherEntryConstantRate apply(FishState state) {

        return new FisherEntryConstantRate(
            growthRateInPercentage.applyAsDouble(state.getRandom()),
            populationName,
            (int) firstYearEntryOccurs.applyAsDouble(state.getRandom())
        );
    }

    /**
     * Getter for property 'growthRateInPercentage'.
     *
     * @return Value for property 'growthRateInPercentage'.
     */
    public DoubleParameter getGrowthRateInPercentage() {
        return growthRateInPercentage;
    }

    /**
     * Setter for property 'growthRateInPercentage'.
     *
     * @param growthRateInPercentage Value to set for property 'growthRateInPercentage'.
     */
    public void setGrowthRateInPercentage(DoubleParameter growthRateInPercentage) {
        this.growthRateInPercentage = growthRateInPercentage;
    }

    /**
     * Getter for property 'populationName'.
     *
     * @return Value for property 'populationName'.
     */
    public String getPopulationName() {
        return populationName;
    }

    /**
     * Setter for property 'populationName'.
     *
     * @param populationName Value to set for property 'populationName'.
     */
    public void setPopulationName(String populationName) {
        this.populationName = populationName;
    }

    /**
     * Getter for property 'firstYearEntryOccurs'.
     *
     * @return Value for property 'firstYearEntryOccurs'.
     */
    public DoubleParameter getFirstYearEntryOccurs() {
        return firstYearEntryOccurs;
    }

    /**
     * Setter for property 'firstYearEntryOccurs'.
     *
     * @param firstYearEntryOccurs Value to set for property 'firstYearEntryOccurs'.
     */
    public void setFirstYearEntryOccurs(DoubleParameter firstYearEntryOccurs) {
        this.firstYearEntryOccurs = firstYearEntryOccurs;
    }
}
