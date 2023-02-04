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

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Redistributes the biomass around according to a "schedule" that maps a simulation step to a grid
 * index. The biomass grids are normalized upon construction, but care must be taken to have the all
 * non-empty grid locations match with {@code SeaTile} that have a BiomassLocal biology, otherwise
 * biomass will be lost. The biomass grids are stored in mutable {@code DoubleGrid2D} fields, but
 * those should never be mutated, so the class is safe to share between parallel simulations. Note
 * that the {@code reallocate} method mutates the tiles biomass arrays directly.
 */
public class BiomassReallocator extends Reallocator<String, BiomassLocalBiology> {

    BiomassReallocator(
        final AllocationGrids<String> allocationGrids
    ) {
        super(allocationGrids, new BiomassAggregator());
    }

    @Override
    public void reallocate(
        final Map<String, DoubleGrid2D> allocationGrids,
        final GlobalBiology globalBiology,
        final List<SeaTile> seaTiles,
        final BiomassLocalBiology aggregatedBiology
    ) {
        final Map<Species, DoubleGrid2D> gridsPerSpecies =
            allocationGrids.entrySet().stream().collect(toImmutableMap(
                entry -> globalBiology.getSpecie(entry.getKey()),
                Entry::getValue
            ));
        final Map<Integer, DoubleGrid2D> indexedBiomassGrids =
            makeNewBiomassGrids(gridsPerSpecies, aggregatedBiology);
        seaTiles
            .stream()
            .filter(seaTile -> seaTile.getBiology() instanceof VariableBiomassBasedBiology)
            .forEach(seaTile -> {
                final double[] biomass =
                    ((VariableBiomassBasedBiology) seaTile.getBiology()).getCurrentBiomass();
                indexedBiomassGrids.forEach(
                    (i, grid) -> biomass[i] = grid.get(seaTile.getGridX(), seaTile.getGridY()));
            });
    }

    private static ImmutableMap<Integer, DoubleGrid2D> makeNewBiomassGrids(
        final Map<Species, DoubleGrid2D> biomassDistributionGridPerSpecies,
        final LocalBiology aggregatedBiology
    ) {
        return biomassDistributionGridPerSpecies
            .entrySet()
            .stream()
            .flatMap(entry -> {
                final Species species = entry.getKey();
                final DoubleGrid2D grid2D = entry.getValue();
                return Stream.of(entry(
                    species.getIndex(),
                    new DoubleGrid2D(grid2D).multiply(aggregatedBiology.getBiomass(species))
                ));
            })
            .collect(toImmutableMap(Entry::getKey, Entry::getValue));
    }

}
