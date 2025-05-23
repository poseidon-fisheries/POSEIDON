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

package uk.ac.ox.oxfish.fisher.heatmap.regression.factory;


import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridXExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridYExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.KernelTransduction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class KernelTransductionFactory implements AlgorithmFactory<KernelTransduction> {


    private DoubleParameter spaceBandwidth = new FixedDoubleParameter(5);


    private DoubleParameter forgettingFactor = new FixedDoubleParameter(1);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @SuppressWarnings("unchecked")
    @Override
    public KernelTransduction apply(final FishState state) {
        final double bandwidth = spaceBandwidth.applyAsDouble(state.getRandom());
        return new KernelTransduction(
            state.getMap(),
            forgettingFactor.applyAsDouble(state.getRandom()),
            entry(new GridXExtractor(), bandwidth),
            entry(new GridYExtractor(), bandwidth)
        );
    }


    public DoubleParameter getSpaceBandwidth() {
        return spaceBandwidth;
    }

    public void setSpaceBandwidth(final DoubleParameter spaceBandwidth) {
        this.spaceBandwidth = spaceBandwidth;
    }

    public DoubleParameter getForgettingFactor() {
        return forgettingFactor;
    }

    public void setForgettingFactor(final DoubleParameter forgettingFactor) {
        this.forgettingFactor = forgettingFactor;
    }
}


