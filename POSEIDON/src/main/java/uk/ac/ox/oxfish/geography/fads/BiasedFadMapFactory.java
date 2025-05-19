/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2022-2025, University of Oxford.
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

import uk.ac.ox.oxfish.geography.currents.BiasedCurrentVectors;
import uk.ac.ox.oxfish.geography.currents.CurrentPatternMapSupplier;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

@SuppressWarnings("unused")
public class BiasedFadMapFactory extends GenericFadMapFactory {

    private DoubleParameter biasY = new FixedDoubleParameter(0);

    private DoubleParameter biasX = new FixedDoubleParameter(0);

    private DoubleParameter gridYMinimum = new FixedDoubleParameter(0);

    private DoubleParameter gridYMaximum = new FixedDoubleParameter(0);

    @SuppressWarnings("unused")
    public BiasedFadMapFactory(final CurrentPatternMapSupplier currentPatternMapSupplier) {
        super(currentPatternMapSupplier);
    }

    @SuppressWarnings("unused")
    public BiasedFadMapFactory() {
    }

    @Override
    protected CurrentVectors buildCurrentVectors(final FishState fishState) {
        return
            new BiasedCurrentVectors(
                super.buildCurrentVectors(fishState),
                biasY.applyAsDouble(fishState.getRandom()),
                biasX.applyAsDouble(fishState.getRandom()),
                (int) gridYMinimum.applyAsDouble(fishState.getRandom()),
                (int) gridYMaximum.applyAsDouble(fishState.getRandom())
            );

    }

    @SuppressWarnings("unused")
    public DoubleParameter getBiasY() {
        return biasY;
    }

    @SuppressWarnings("unused")
    public void setBiasY(final DoubleParameter biasY) {
        this.biasY = biasY;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getBiasX() {
        return biasX;
    }

    @SuppressWarnings("unused")
    public void setBiasX(final DoubleParameter biasX) {
        this.biasX = biasX;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getGridYMinimum() {
        return gridYMinimum;
    }

    @SuppressWarnings("unused")
    public void setGridYMinimum(final DoubleParameter gridYMinimum) {
        this.gridYMinimum = gridYMinimum;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getGridYMaximum() {
        return gridYMaximum;
    }

    @SuppressWarnings("unused")
    public void setGridYMaximum(final DoubleParameter gridYMaximum) {
        this.gridYMaximum = gridYMaximum;
    }
}
