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

package uk.ac.ox.oxfish.biology.complicated;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.common.primitives.ImmutableDoubleArray;
import java.util.Collection;
import java.util.List;

/**
 * Tuna-style meristics track male and females separately.
 */
@SuppressWarnings("UnstableApiUsage")
public class TunaMeristics implements Meristics {

    public static final int NUMBER_OF_SUBDIVISIONS = 2; // MALE and FEMALE
    private final int numberOfBins;
    private final List<ImmutableDoubleArray> weights;
    private final List<ImmutableDoubleArray> lengths;

    /**
     * Constructs a new TunaMeristics object.
     *
     * @param weights An iterable of arrays of weights, where the first array is the weights of the
     *                females and the second array is the weights of the males.
     * @param lengths An iterable of arrays of weights, where the first array is the weights of the
     *                females and the second array is the weights of the males.
     */
    public TunaMeristics(
        final Iterable<double[]> weights,
        final Iterable<double[]> lengths
    ) {
        this(
            stream(weights).map(ImmutableDoubleArray::copyOf).collect(toImmutableList()),
            stream(lengths).map(ImmutableDoubleArray::copyOf).collect(toImmutableList())
        );
    }

    /**
     * Constructs a new TunaMeristics object.
     *
     * @param weights A collection of immutable arrays of weights, where the first array is the
     *                weights of the females and the second array is the weights of the males.
     * @param lengths An collection of immutable iterable of arrays of weights, where the first
     *                array is the weights of the females and the second array is the weights of the
     *                males.
     */
    public TunaMeristics(
        final Collection<ImmutableDoubleArray> weights,
        final Collection<ImmutableDoubleArray> lengths
    ) {
        checkArgument(weights.size() == NUMBER_OF_SUBDIVISIONS);
        checkArgument(lengths.size() == NUMBER_OF_SUBDIVISIONS);
        final int[] arraySizes = Streams
            .concat(weights.stream(), lengths.stream())
            .mapToInt(ImmutableDoubleArray::length)
            .distinct()
            .toArray();
        checkArgument(arraySizes.length == 1);
        this.numberOfBins = arraySizes[0];
        this.weights = ImmutableList.copyOf(weights);
        this.lengths = ImmutableList.copyOf(lengths);
    }

    @Override
    public double getLength(final int subdivision, final int bin) {
        return lengths.get(subdivision).get(bin);
    }

    @Override
    public double getWeight(final int subdivision, final int bin) {
        return weights.get(subdivision).get(bin);
    }

    @Override
    public int getNumberOfSubdivisions() {
        return NUMBER_OF_SUBDIVISIONS;
    }

    @Override
    public int getNumberOfBins() {
        return numberOfBins;
    }

    @Override
    public double getLengthAtAge(final int ageInYears, final int subdivision) {
        throw new UnsupportedOperationException();
    }
}
