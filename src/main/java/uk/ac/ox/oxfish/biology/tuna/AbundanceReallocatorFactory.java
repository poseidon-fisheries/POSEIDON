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

package uk.ac.ox.oxfish.biology.tuna;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.LARGE;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.SMALL;

import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.IntFunction;
import uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Creates an {@link AbundanceReallocator}. The mildly peculiar thing here is the {@code
 * firstLargeBinPerSpecies} map, which is used to create the "bin to size category" mapping. It's
 * just a way to tell the factory where the cutoff between "small" and "large" is and let the factory
 * build the relevant function. Note that this is very tuna specific. We'd need to make this class a
 * bit more general if it was to be used with different groupings.
 */
public class AbundanceReallocatorFactory extends ReallocatorFactory<AbundanceReallocator> {

    private Map<String, Integer> firstLargeBinPerSpecies;

    /**
     * Empty constructor needed for YAML.
     */
    @SuppressWarnings("unused")
    public AbundanceReallocatorFactory() {
    }

    public AbundanceReallocatorFactory(
        final Path biomassDistributionsFilePath,
        final Map<String, Integer> firstLargeBinPerSpecies,
        final int period
    ) {
        super(biomassDistributionsFilePath, period);
        this.firstLargeBinPerSpecies = ImmutableMap.copyOf(firstLargeBinPerSpecies);
    }

    @SuppressWarnings("unused")
    public Map<String, Integer> getFirstLargeBinPerSpecies() {
        return ImmutableMap.copyOf(firstLargeBinPerSpecies);
    }

    @SuppressWarnings("unused")
    public void setFirstLargeBinPerSpecies(final Map<String, Integer> firstLargeBinPerSpecies) {
        this.firstLargeBinPerSpecies = ImmutableMap.copyOf(firstLargeBinPerSpecies);
    }

    @Override
    public AbundanceReallocator apply(final FishState fishState) {
        checkNotNull(getSpeciesCodes(), "Need to call setSpeciesCodes() before using");
        checkNotNull(getMapExtent(), "Need to call setMapExtent() before using");
        final AllocationGrids<Entry<String, SizeGroup>> grids =
            new SmallLargeAllocationGridsSupplier(
                getSpeciesCodes(),
                getBiomassDistributionsFilePath(),
                getMapExtent(),
                365
            ).get();

        final Map<String, IntFunction<SizeGroup>> binToSizeGroupMappings =
            firstLargeBinPerSpecies.entrySet().stream().collect(toImmutableMap(
                Entry::getKey,
                entry -> bin -> bin >= entry.getValue() ? LARGE : SMALL
            ));

        return new AbundanceReallocator(grids, binToSizeGroupMappings);
    }
}
