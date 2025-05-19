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

package uk.ac.ox.oxfish.model.data.monitors.accumulators;

import com.google.common.primitives.ImmutableDoubleArray;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class KsTestAccumulator implements Accumulator<Double> {

    // having this be static means that we share a single RNG, but this
    // should not matter as long as we only use deterministic methods
    private static final KolmogorovSmirnovTest kolmogorovSmirnovTest =
        new KolmogorovSmirnovTest();

    private final ImmutableDoubleArray.Builder arrayBuilder = ImmutableDoubleArray.builder();
    private final Map<Integer, double[]> referenceDistributionPerYear;

    public KsTestAccumulator(final Map<Integer, double[]> referenceDistributionPerYear) {
        checkArgument(
            referenceDistributionPerYear.values().stream().allMatch(a -> a.length > 1),
            "Reference distributions must contain more than one element"
        );
        this.referenceDistributionPerYear =
            referenceDistributionPerYear
                .entrySet()
                .stream()
                .collect(toImmutableMap(
                    Entry::getKey,
                    entry -> entry.getValue().clone()
                ));
    }

    @Override
    public String getNameFormat() {
        return "K.-S. test statistic for %s";
    }

    @Override
    public void accumulate(final Double value) {
        arrayBuilder.add(value);
    }

    @Override
    public double applyAsDouble(final FishState fishState) {
        final double[] simulatedDistribution = arrayBuilder.build().toArray();
        return simulatedDistribution.length < 2
            ? 1.0 // if we don't have enough events to do a proper test, we assume the distributions are different
            : kolmogorovSmirnovTest.kolmogorovSmirnovStatistic(
                simulatedDistribution,
                referenceDistributionPerYear.get(fishState.getCalendarYear())
            );
    }
}
