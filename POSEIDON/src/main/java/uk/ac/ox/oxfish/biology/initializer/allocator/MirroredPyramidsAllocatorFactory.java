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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class MirroredPyramidsAllocatorFactory implements AlgorithmFactory<MirroredPyramidsAllocator> {


    private final SinglePeakAllocatorFactory delegate = new SinglePeakAllocatorFactory();


    private DoubleParameter noiseLevel = new FixedDoubleParameter(0.1);


    @Override
    public MirroredPyramidsAllocator apply(final FishState state) {
        return new MirroredPyramidsAllocator(
            delegate.apply(state),
            noiseLevel.applyAsDouble(state.getRandom())
        );
    }


    public DoubleParameter getNoiseLevel() {
        return noiseLevel;
    }

    public void setNoiseLevel(final DoubleParameter noiseLevel) {
        this.noiseLevel = noiseLevel;
    }

    public DoubleParameter getPeakX() {
        return delegate.getPeakX();
    }

    public void setPeakX(final DoubleParameter peakX) {
        delegate.setPeakX(peakX);
    }

    public DoubleParameter getPeakY() {
        return delegate.getPeakY();
    }

    public void setPeakY(final DoubleParameter peakY) {
        delegate.setPeakY(peakY);
    }

    public DoubleParameter getSmoothingValue() {
        return delegate.getSmoothingValue();
    }

    public void setSmoothingValue(final DoubleParameter smoothingValue) {
        delegate.setSmoothingValue(smoothingValue);
    }

    public int getMaxSpread() {
        return delegate.getMaxSpread();
    }

    public void setMaxSpread(final int maxSpread) {
        delegate.setMaxSpread(maxSpread);
    }

    public DoubleParameter getPeakBiomass() {
        return delegate.getPeakBiomass();
    }

    public void setPeakBiomass(final DoubleParameter peakBiomass) {
        delegate.setPeakBiomass(peakBiomass);
    }
}
