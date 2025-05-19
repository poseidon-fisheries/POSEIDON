/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

public class FadZapperFactory implements AlgorithmFactory<FadZapper> {
    private DoubleParameter maxFadAge;
    private IntegerParameter minGridX;

    @SuppressWarnings("unused")
    public FadZapperFactory() {
    }

    public FadZapperFactory(
        final DoubleParameter maxFadAge,
        final IntegerParameter minGridX
    ) {
        this.maxFadAge = maxFadAge;
        this.minGridX = minGridX;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getMaxFadAge() {
        return maxFadAge;
    }

    public void setMaxFadAge(final DoubleParameter maxFadAge) {
        this.maxFadAge = maxFadAge;
    }

    @SuppressWarnings("unused")
    public IntegerParameter getMinGridX() {
        return minGridX;
    }

    @SuppressWarnings("unused")
    public void setMinGridX(final IntegerParameter minGridX) {
        this.minGridX = minGridX;
    }

    @Override
    public FadZapper apply(final FishState fishState) {
        final double maxFadAge = this.maxFadAge.applyAsDouble(fishState.getRandom());
        return new FadZapper(fad ->
            fad.getLocation().getGridX() <= minGridX.getValue() ||
                fishState.getStep() - fad.getStepDeployed() > maxFadAge
        );
    }
}
