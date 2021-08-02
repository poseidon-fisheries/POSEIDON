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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.biology.initializer.allocator.SmallLargeAllocationGridsSupplier.SizeGroup.LARGE;
import static uk.ac.ox.oxfish.biology.initializer.allocator.SmallLargeAllocationGridsSupplier.SizeGroup.SMALL;

import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.IntFunction;
import uk.ac.ox.oxfish.biology.initializer.allocator.SmallLargeAllocationGridsSupplier.SizeGroup;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFishState;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class ScheduledAbundanceReallocatorFactory
    extends ReallocatorFactory implements
    AlgorithmFactory<ScheduledAbundanceReallocator> {

    private Map<String, Integer> firstLargeBinPerSpecies;
    private MapExtent mapExtent;

    /**
     * Empty constructor to allow YAML instantiation.
     */
    @SuppressWarnings("unused")
    public ScheduledAbundanceReallocatorFactory() {
    }

    public ScheduledAbundanceReallocatorFactory(
        final Path speciesCodesFilePath,
        final Path biomassDistributionsFilePath,
        final int period,
        final Map<String, Integer> firstLargeBinPerSpecies
    ) {
        super(speciesCodesFilePath, biomassDistributionsFilePath, period);
        this.firstLargeBinPerSpecies = ImmutableMap.copyOf(firstLargeBinPerSpecies);
    }

    public MapExtent getMapExtent() {
        return mapExtent;
    }

    public void setMapExtent(final MapExtent mapExtent) {
        this.mapExtent = mapExtent;
    }

    @Override
    public ScheduledAbundanceReallocator apply(final FishState fishState) {
        checkNotNull(mapExtent, "Need to call setMapExtent() before using");
        final AllocationGrids<Entry<String, SizeGroup>> grids =
            new SmallLargeAllocationGridsSupplier(
                getSpeciesCodesFilePath(),
                getBiomassDistributionsFilePath(),
                this.mapExtent,
                getPeriod()
            ).get();
        final Map<String, IntFunction<SizeGroup>> binToSizeGroupMappings =
            firstLargeBinPerSpecies.entrySet().stream().collect(toImmutableMap(
                Entry::getKey,
                entry -> bin -> bin >= entry.getValue() ? LARGE : SMALL
            ));
        return new ScheduledAbundanceReallocator(
            new AbundanceReallocator(grids, binToSizeGroupMappings),
            new AbundanceAggregator(),
            grids.getSchedule()
        );
    }
}
