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

import com.google.common.collect.Lists;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.ArrayList;

public class SinglePeakAllocatorFactory implements AlgorithmFactory<PyramidsAllocator> {


    private DoubleParameter peakX = new FixedDoubleParameter(1);
    private DoubleParameter peakY = new FixedDoubleParameter(1);
    private DoubleParameter smoothingValue = new FixedDoubleParameter(.7d);

    private int maxSpread = 6;

    private DoubleParameter peakBiomass = new FixedDoubleParameter(1);


    public SinglePeakAllocatorFactory() {
    }

    @Override
    public PyramidsAllocator apply(final FishState fishState) {
        final ArrayList<int[]> singlePeak = Lists.newArrayList();
        final int peakX = Math.toIntExact(Math.round(this.peakX.applyAsDouble(fishState.getRandom())));
        final int peakY = Math.toIntExact(Math.round(this.peakY.applyAsDouble(fishState.getRandom())));
        singlePeak.add(new int[]{peakX, peakY});
        return new PyramidsAllocator(
            smoothingValue.applyAsDouble(fishState.getRandom()),
            maxSpread,
            peakBiomass.applyAsDouble(fishState.getRandom()),
            singlePeak
        );
    }

    public DoubleParameter getPeakX() {
        return peakX;
    }

    public void setPeakX(final DoubleParameter peakX) {
        this.peakX = peakX;
    }

    public DoubleParameter getPeakY() {
        return peakY;
    }

    public void setPeakY(final DoubleParameter peakY) {
        this.peakY = peakY;
    }

    public DoubleParameter getSmoothingValue() {
        return smoothingValue;
    }

    public void setSmoothingValue(final DoubleParameter smoothingValue) {
        this.smoothingValue = smoothingValue;
    }

    public int getMaxSpread() {
        return maxSpread;
    }

    public void setMaxSpread(final int maxSpread) {
        this.maxSpread = maxSpread;
    }

    public DoubleParameter getPeakBiomass() {
        return peakBiomass;
    }

    public void setPeakBiomass(final DoubleParameter peakBiomass) {
        this.peakBiomass = peakBiomass;
    }
}
