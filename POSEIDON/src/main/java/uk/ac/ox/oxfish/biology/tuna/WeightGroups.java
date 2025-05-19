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

package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.ImmutableDoubleArray;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.zip;
import static java.lang.Math.toIntExact;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.IntStream.range;

@SuppressWarnings("UnstableApiUsage")
public class WeightGroups {

    static final WeightGroups SINGLE_GROUP = new WeightGroups(ImmutableList.of("all"), ImmutableList.of());

    private final List<String> groupNames;
    private final ImmutableDoubleArray cutoffs;

    public WeightGroups(Collection<String> groupNames, Collection<Double> cutoffs) {
        checkArgument(groupNames.size() == cutoffs.size() + 1);
        checkArgument(cutoffs.stream().allMatch(v -> v > 0));
        checkArgument(zip( // check that cutoffs are monotonically increasing
            cutoffs.stream(),
            cutoffs.stream().skip(1),
            (a, b) -> b > a
        ).allMatch(Boolean::booleanValue));

        this.groupNames = ImmutableList.copyOf(groupNames);
        this.cutoffs = ImmutableDoubleArray.copyOf(cutoffs);
    }

    public Map<String, List<Integer>> getBinsPerGroup(
        double[] weights
    ) {
        return range(0, weights.length).boxed()
            .collect(collectingAndThen(
                groupingBy(
                    bin -> groupNames.get(toIntExact(
                        cutoffs.stream()
                            .filter(cutoff -> cutoff <= weights[bin])
                            .count()
                    )),
                    toImmutableList()
                ),
                ImmutableMap::copyOf
            ));
    }
}
