/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.IntStream.range;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.ImmutableDoubleArray;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

@SuppressWarnings("UnstableApiUsage")

public class NonMutatingArrayFilter extends ArrayFilter {

    public NonMutatingArrayFilter(final Collection<Collection<Double>> filters) {
        super(false,
                convertCollectionToPOJOArray(filters));
    }
    public NonMutatingArrayFilter(double[]... filters) {
        super(false,
                filters);
    }

    /**
     * the way I think we are using NonMutatingArrayFilter is basically as a fixed selectivity grid but with the additional
     * assumption
     * tht the abundance matrix passed here is not modified as a side effect (which is different from what happens in ArrayFilter!)
     * @param species the species of fish
     * @param abundance
     * @return
     */
    @Override
    public double[][] filter(Species species, double[][] abundance) {
        return super.filter(species, Arrays.stream(abundance).map(double[]::clone).toArray(double[][]::new));
    }
}
