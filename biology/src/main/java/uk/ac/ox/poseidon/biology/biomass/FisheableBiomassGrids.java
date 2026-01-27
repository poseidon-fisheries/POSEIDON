/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.biology.biomass;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import sim.util.Int2D;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.biology.FisheableGrid;
import uk.ac.ox.poseidon.biology.buckets.BiomassBucket;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.biology.species.SpeciesIndex;
import uk.ac.ox.poseidon.biology.species.SpeciesIndexed;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

public class FisheableBiomassGrids implements FisheableGrid, SpeciesIndexed {

    @Getter
    private final SpeciesIndex speciesIndex;
    private final BiomassGrid[] grids;

    public FisheableBiomassGrids(final Collection<? extends BiomassGrid> grids) {
        final ImmutableMap<Species, ? extends BiomassGrid> gridMap =
            grids.stream().collect(toImmutableMap(
                BiomassGrid::getSpecies,
                identity()
            ));
        if (gridMap.size() != grids.size()) throw new IllegalArgumentException(
            "Duplicate species %s found in collection of biomass grids: %s".formatted(
                grids
                    .stream()
                    .map(BiomassGrid::getSpecies)
                    .collect(groupingBy(Function.identity(), counting()))
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet()),
                grids
            )
        );
        this.speciesIndex = SpeciesIndex.of(gridMap.keySet());
        this.grids = new BiomassGrid[gridMap.size()];
        speciesIndex.asMap().forEach((species, index) ->
            this.grids[index] = gridMap.get(species)
        );
    }

    @Override
    public Fisheable getFisheableCell(final Int2D cell) {
        return new FisheableCell(cell);
    }

    @RequiredArgsConstructor
    class FisheableCell implements Fisheable {

        private final Int2D cell;

        @Override
        public Bucket extract(final Bucket fishToExtract) {
            final double[] biomassExtracted = speciesIndex.newDoubleArray();
            switch (fishToExtract) {
                case final BiomassBucket biomassBucket when sameIndex(biomassBucket) -> {
                    for (int i = 0; i < speciesIndex.size(); i++) {
                        extractBiomass(i, biomassBucket.getDouble(i), biomassExtracted);
                    }
                }
                default -> fishToExtract.forEachBiomassValue((species, biomass) -> {
                    final int i = speciesIndex.indexOf(species);
                    if (i != -1) extractBiomass(i, biomass, biomassExtracted);
                });
            }
            return BiomassBucket.of(biomassExtracted, speciesIndex);
        }

        private void extractBiomass(
            final int gridIndex,
            final double biomassToExtract,
            final double[] biomassExtracted
        ) {
            final BiomassGrid grid = grids[gridIndex];
            final double currentBiomass = grid.getDouble(cell);
            final double extractedBiomass = Math.min(biomassToExtract, currentBiomass);
            biomassExtracted[gridIndex] = extractedBiomass;
            grid.setBiomass(cell, currentBiomass - extractedBiomass);
        }

        @Override
        public Bucket availableFish() {
            final double[] biomasses = new double[speciesIndex.size()];
            for (int i = 0; i < grids.length; i++) {
                biomasses[i] = grids[i].getDouble(cell);
            }
            return BiomassBucket.of(biomasses, speciesIndex);
        }

        @Override
        public void release(@NonNull final Bucket fishToRelease) {
            switch (fishToRelease) {
                case final BiomassBucket biomassBucket when sameIndex(biomassBucket) -> {
                    for (int i = 0; i < speciesIndex.size(); i++) {
                        final BiomassGrid grid = grids[i];
                        grid.setBiomass(cell, grid.getDouble(cell) + biomassBucket.getDouble(i));
                    }
                }
                default -> fishToRelease.forEachBiomassValue((species, biomass) -> {
                    final int i = speciesIndex.indexOf(species);
                    if (i == -1) throw new IllegalArgumentException(
                        "No grid available to release %s.".formatted(species)
                    );
                    final BiomassGrid grid = grids[i];
                    grid.setBiomass(cell, grid.getDouble(cell) + biomass);
                });
            }
        }
    }
}
