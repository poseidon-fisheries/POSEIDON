/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.stream;

import uk.ac.ox.oxfish.biology.Species;

public class NonMutatingProportionFilter
    implements AbundanceFilter {

    private final double proportion;

    public NonMutatingProportionFilter(final double proportion) {
        checkArgument(proportion >= 0 && proportion <= 1, proportion);
        this.proportion = proportion;
    }

    @Override
    public double[][] filter(final Species species, final double[][] abundance) {
        return stream(abundance)
            .map(subArray -> stream(subArray).map(d -> d * proportion).toArray())
            .toArray(double[][]::new);
    }
}
