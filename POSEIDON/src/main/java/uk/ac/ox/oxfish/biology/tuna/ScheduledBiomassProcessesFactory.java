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
import static java.util.function.Function.identity;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Map;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Factory that builds a {@link ScheduledBiologicalProcesses} for {@link BiomassLocalBiology}.
 * Aggregation and reallocation of the biomass (excluding FADs) are the only processes handled
 * here.
 */
public class ScheduledBiomassProcessesFactory
    implements AlgorithmFactory<ScheduledBiologicalProcesses<BiomassLocalBiology>> {

    private BiomassReallocator biomassReallocator;


    @Override
    public ScheduledBiologicalProcesses<BiomassLocalBiology> apply(final FishState fishState) {

        checkNotNull(
            biomassReallocator,
            "setBiomassReallocator must be called before using."
        );

        // The biomass scheduled processes are pretty straightforward:
        // we aggregate the biomass from the ocean (not the FADs) and
        // we redistribute it across the map.
        final Collection<BiologicalProcess<BiomassLocalBiology>> biologicalProcesses =
            ImmutableList.of(
                new BiomassExtractor(false, true),
                getBiomassReallocator()
            );

        final AllocationGrids<String> grids =
            getBiomassReallocator().getAllocationGrids();

        final Map<Integer, Collection<BiologicalProcess<BiomassLocalBiology>>> schedule =
            grids.getGrids()
                .keySet()
                .stream()
                .collect(toImmutableMap(identity(), step -> biologicalProcesses));

        return new ScheduledBiologicalProcesses<>(
            grids.getStepMapper(),
            schedule
        );
    }

    private BiomassReallocator getBiomassReallocator() {
        return biomassReallocator;
    }

    public void setBiomassReallocator(final BiomassReallocator biomassReallocator) {
        this.biomassReallocator = biomassReallocator;
    }
}