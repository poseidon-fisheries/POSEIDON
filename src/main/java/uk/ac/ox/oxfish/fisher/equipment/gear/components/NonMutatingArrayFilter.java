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

import com.google.common.primitives.ImmutableDoubleArray;
import java.util.Collection;
import java.util.List;
import uk.ac.ox.oxfish.biology.Species;

@SuppressWarnings("UnstableApiUsage")
public class NonMutatingArrayFilter implements AbundanceFilter {

    private final List<ImmutableDoubleArray> filters;

    public NonMutatingArrayFilter(final Collection<Collection<Double>> filters) {
        this.filters = filters.stream()
            .map(ImmutableDoubleArray::copyOf)
            .collect(toImmutableList());
    }

    public List<ImmutableDoubleArray> getFilters() {
        return filters;
    }

    @Override
    public double[][] filter(final Species species, final double[][] abundance) {
        return range(0, abundance.length).mapToObj(subdivision ->
            range(0, abundance[subdivision].length).mapToDouble(age ->
                abundance[subdivision][age] * filters.get(subdivision).get(age)
            ).toArray()
        ).toArray(double[][]::new);
    }

}
