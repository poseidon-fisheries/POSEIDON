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

package uk.ac.ox.oxfish.fisher.heatmap.acquisition.factory;

import uk.ac.ox.oxfish.fisher.heatmap.acquisition.ExhaustiveAcquisitionFunction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/5/16.
 */
public class ExhaustiveAcquisitionFunctionFactory implements AlgorithmFactory<ExhaustiveAcquisitionFunction> {


    private DoubleParameter proportionSearched = new FixedDoubleParameter(1d);
    private boolean ignoreProtectedAreas = true;
    private boolean ignoreWastelands = true;

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ExhaustiveAcquisitionFunction apply(final FishState state) {
        return new ExhaustiveAcquisitionFunction(
            proportionSearched.applyAsDouble(state.getRandom()),
            ignoreProtectedAreas,
            ignoreWastelands
        );
    }


    public DoubleParameter getProportionSearched() {
        return proportionSearched;
    }

    public void setProportionSearched(final DoubleParameter proportionSearched) {
        this.proportionSearched = proportionSearched;
    }

    /**
     * Getter for property 'ignoreProtectedAreas'.
     *
     * @return Value for property 'ignoreProtectedAreas'.
     */
    public boolean isIgnoreProtectedAreas() {
        return ignoreProtectedAreas;
    }

    /**
     * Setter for property 'ignoreProtectedAreas'.
     *
     * @param ignoreProtectedAreas Value to set for property 'ignoreProtectedAreas'.
     */
    public void setIgnoreProtectedAreas(final boolean ignoreProtectedAreas) {
        this.ignoreProtectedAreas = ignoreProtectedAreas;
    }

    /**
     * Getter for property 'ignoreWastelands'.
     *
     * @return Value for property 'ignoreWastelands'.
     */
    public boolean isIgnoreWastelands() {
        return ignoreWastelands;
    }

    /**
     * Setter for property 'ignoreWastelands'.
     *
     * @param ignoreWastelands Value to set for property 'ignoreWastelands'.
     */
    public void setIgnoreWastelands(final boolean ignoreWastelands) {
        this.ignoreWastelands = ignoreWastelands;
    }
}
