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

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.IntFunction;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.initializer.allocator.SmallLargeAllocationGridsSupplier.SizeGroup;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 *
 */
public class AbundanceReallocator extends Reallocator<Entry<String, SizeGroup>> {


    private final Map<String, ? extends IntFunction<SizeGroup>> binToSizeGroupMappings;

    /**
     * Constructs a new AbundanceReallocator.
     *
     * @param allocationGrids        The distribution grids used to reallocate biomass
     * @param period                 The number to use as modulo for looping the schedule (usually
     *                               365)
     * @param binToSizeGroupMappings A map from species names to a function giving us the size group
     *                               (small or large) for each age bin
     */
    AbundanceReallocator(
        final AllocationGrids<Entry<String, SizeGroup>> allocationGrids,
        final int period,
        final Map<String, ? extends IntFunction<SizeGroup>> binToSizeGroupMappings
    ) {
        super(allocationGrids, period);
        this.binToSizeGroupMappings = ImmutableMap.copyOf(binToSizeGroupMappings);
    }

    @Override
    void performReallocation(
        final GlobalBiology globalBiology,
        final NauticalMap nauticalMap,
        final Map<Entry<String, SizeGroup>, ? extends DoubleGrid2D> grids
    ) {
        final Map<SeaTile, AbundanceLocalBiology> seaTileBiologies =
            getSeaTileBiologies(nauticalMap);
        final Map<Species, double[][]> aggregatedAbundanceBefore =
            aggregateAbundance(globalBiology, seaTileBiologies);
        performReallocation(
            grids,
            seaTileBiologies,
            aggregatedAbundanceBefore
        );
    }

    private static Map<SeaTile, AbundanceLocalBiology> getSeaTileBiologies(
        final NauticalMap nauticalMap
    ) {
        return nauticalMap.getAllSeaTilesExcludingLandAsList()
            .stream()
            .filter(seaTile -> seaTile.getBiology() instanceof AbundanceLocalBiology)
            .collect(toImmutableMap(
                identity(),
                seaTile -> ((AbundanceLocalBiology) seaTile.getBiology())
            ));
    }

    private static Map<Species, double[][]> aggregateAbundance(
        final GlobalBiology globalBiology,
        final Map<SeaTile, AbundanceLocalBiology> seaTileBiologies
    ) {

        // Create a map from species to empty abundance arrays which we
        // are going to mutate directly when summing up global abundance
        final Map<Species, double[][]> abundances = globalBiology.getSpecies()
            .stream()
            .collect(toImmutableMap(
                identity(),
                species -> new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()]
            ));

        seaTileBiologies.forEach((seaTile, localBiology) ->
            abundances.forEach((species, abundance) ->
                range(0, abundance.length).forEach(subdivision ->
                    range(0, abundance[subdivision].length).forEach(bin ->
                        abundance[subdivision][bin] +=
                            localBiology.getAbundance(species).getAbundance(subdivision, bin)
                    )
                )
            )
        );
        return abundances;
    }

    private void performReallocation(
        final Map<Entry<String, SizeGroup>, ? extends DoubleGrid2D> grids,
        final Map<SeaTile, AbundanceLocalBiology> seaTileBiologies,
        final Map<Species, double[][]> aggregatedAbundancePerSpecies
    ) {
        aggregatedAbundancePerSpecies.forEach((species, aggregatedAbundance) ->
            range(0, aggregatedAbundance.length).forEach(subdivision ->
                range(0, aggregatedAbundance[subdivision].length).forEach(bin -> {
                    final DoubleGrid2D grid = grids.get(entry(
                        species.getName(),
                        binToSizeGroupMappings.get(species.getName()).apply(bin)
                    ));
                    final double numFish = aggregatedAbundance[subdivision][bin];
                    seaTileBiologies.forEach((seaTile, localBiology) ->
                        localBiology.getAbundance(species).asMatrix()[subdivision][bin] =
                            numFish * grid.get(seaTile.getGridX(), seaTile.getGridY())
                    );
                })
            )
        );
    }

    public void reallocateAtStep(
        final int step,
        final Map<Species, double[][]> aggregatedAbundance,
        final NauticalMap nauticalMap
    ) {

        final Map<Entry<String, SizeGroup>, DoubleGrid2D> grids = allocationGrids
            .atOrBeforeStep(step % period)
            .orElseThrow(() -> new IllegalStateException("Grids not found for step: " + step));

        performReallocation(
            grids,
            getSeaTileBiologies(nauticalMap),
            aggregatedAbundance
        );

    }

}