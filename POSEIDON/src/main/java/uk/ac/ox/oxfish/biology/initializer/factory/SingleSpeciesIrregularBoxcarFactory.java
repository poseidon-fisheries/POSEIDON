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

package uk.ac.ox.oxfish.biology.initializer.factory;

import com.beust.jcommander.internal.Lists;
import uk.ac.ox.oxfish.biology.boxcars.EquallySpacedBertalanffyFactory;
import uk.ac.ox.oxfish.biology.complicated.GrowthBinByList;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

/**
 * Boxcar factory where boxes are not necessarilly all of the same dimension.
 */
public class SingleSpeciesIrregularBoxcarFactory extends SingleSpeciesBoxcarAbstractFactory {

    /**
     * these would represent the "mid-point" of lengths that we are going to model. Fish would transition from
     * one to the other
     */
    private List<Double> binnedLengthsInCm = Lists.newArrayList(10d, 30d, 60d);


    @Override
    protected GrowthBinByList generateBins(final FishState state) {
        final double[] lengths = new double[binnedLengthsInCm.size()];
        final double[] weights = new double[binnedLengthsInCm.size()];

        final Double alpha = getAllometricAlpha().applyAsDouble(state.getRandom());
        final Double beta = getAllometricBeta().applyAsDouble(state.getRandom());

        for (int bin = 0; bin < lengths.length; bin++) {
            lengths[bin] = binnedLengthsInCm.get(bin);

            weights[bin] = EquallySpacedBertalanffyFactory.bertnalanffyLengthToWeight(
                alpha,
                beta,
                lengths[bin]
            );
        }
        return new GrowthBinByList(
            1,
            lengths,
            weights,
            EquallySpacedBertalanffyFactory.bertalanffyLengthAtAge(
                getLInfinity().applyAsDouble(state.getRandom()),
                0,
                getK().applyAsDouble(state.getRandom()),
                EquallySpacedBertalanffyFactory.MAXIMUM_AGE_TRACKED
            )


        );
    }


    public List<Double> getBinnedLengthsInCm() {
        return binnedLengthsInCm;
    }

    public void setBinnedLengthsInCm(final List<Double> binnedLengthsInCm) {
        this.binnedLengthsInCm = binnedLengthsInCm;
    }
}
