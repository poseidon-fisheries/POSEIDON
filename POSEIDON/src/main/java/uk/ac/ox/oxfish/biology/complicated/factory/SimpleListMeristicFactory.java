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

package uk.ac.ox.oxfish.biology.complicated.factory;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.List;

/**
 * a simpler version of WeightMeristics (which always assumed the sex split);
 * Here we simply ask a list of lengths, weights and maturities and assume there is no male/female split....
 */
public class SimpleListMeristicFactory implements AlgorithmFactory<FromListMeristics> {


    private List<Double> weights = Lists.newArrayList(10d, 20d, 30d);

    private List<Double> lengths = Lists.newArrayList(10d, 20d, 30d);


    @Override
    public FromListMeristics apply(FishState fishState) {
        Preconditions.checkArgument(weights.size() > 0);
        Preconditions.checkArgument(weights.size() == lengths.size(), "lengths-weights are lists of different size!");


        return new FromListMeristics(
            Doubles.toArray(weights),
            Doubles.toArray(lengths),
            1
        );

    }

    public List<Double> getWeights() {
        return weights;
    }

    public void setWeights(List<Double> weights) {
        this.weights = weights;
    }

    public List<Double> getLengths() {
        return lengths;
    }

    public void setLengths(List<Double> lengths) {
        this.lengths = lengths;
    }


}
