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
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.Streams.stream;
import static java.util.stream.IntStream.range;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.ImmutableDoubleArray;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import uk.ac.ox.oxfish.biology.Species;

@SuppressWarnings("UnstableApiUsage")
public class ImmutableAbundance {

    private final Species species;
    private final ImmutableList<ImmutableDoubleArray> abundance;

    public ImmutableAbundance(
        final StructuredAbundance abundance,
        final Species species
    ) {
        this(species, abundance.asMatrix());
    }

    public ImmutableAbundance(
        final Species species,
        final double[][] abundance
    ) {
        this(
            species, Arrays.stream(abundance)
                .map(ImmutableDoubleArray::copyOf)
                .collect(toImmutableList())
        );
    }

    public ImmutableAbundance(
        final Species species,
        final Collection<ImmutableDoubleArray> abundance
    ) {
        this.species = species;
        this.abundance = ImmutableList.copyOf(abundance);
    }

    public ImmutableAbundance(
        final Species species,
        final Iterable<Iterable<Double>> abundance
    ) {
        this(
            species,
            stream(abundance).map(ImmutableDoubleArray::copyOf).collect(toImmutableList())
        );
    }

    public static ImmutableAbundance empty(final Species species) {
        return new ImmutableAbundance(
            species,
            Stream
                .generate(() -> ImmutableDoubleArray.copyOf(new double[species.getNumberOfBins()]))
                .limit(species.getNumberOfSubdivisions())
                .collect(toImmutableList())
        );
    }

    public static Map<Species, ImmutableAbundance> extractFrom(
        final AbundanceLocalBiology biology
    ) {
        return biology
            .getAbundance()
            .entrySet()
            .stream()
            .collect(toImmutableMap(
                Entry::getKey,
                entry -> new ImmutableAbundance(entry.getKey(), entry.getValue())
            ));
    }

    public List<ImmutableDoubleArray> getAbundance() {
        return abundance;
    }

    public ImmutableAbundance add(final ImmutableAbundance other) {
        checkArgument(this.species == other.species);
        return add(
            other.abundance.stream()
                .map(ImmutableDoubleArray::toArray)
                .toArray(double[][]::new)
        );
    }

    public ImmutableAbundance add(final double[][] other) {
        final ImmutableList<ImmutableDoubleArray> abundance =
            range(0, this.abundance.size())
                .mapToObj(sub -> ImmutableDoubleArray.copyOf(
                    range(0, this.abundance.get(sub).length()).mapToDouble(bin ->
                        this.abundance.get(sub).get(bin) + other[sub][bin]
                    )
                ))
                .collect(toImmutableList());
        return new ImmutableAbundance(species, abundance);
    }

    public double[][] asMatrix() {
        return abundance.stream().map(ImmutableDoubleArray::toArray).toArray(double[][]::new);
    }

}
