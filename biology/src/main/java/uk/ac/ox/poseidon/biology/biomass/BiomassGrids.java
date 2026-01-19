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
import sim.util.Int2D;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.BucketBuilder;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.biology.FisheableGrid;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

public class BiomassGrids implements FisheableGrid {

    private final Map<Species, BiomassGrid> grids;

    public BiomassGrids(final Collection<? extends BiomassGrid> grids) {
        this(
            grids.stream().collect(toImmutableMap(
                BiomassGrid::getSpecies,
                identity()
            ))
        );
    }

    private BiomassGrids(final Map<Species, BiomassGrid> grids) {
        this.grids = ImmutableMap.copyOf(grids);
    }

    @Override
    public Fisheable getFisheableCell(final Int2D cell) {
        return new FisheableCell(cell);
    }

    class FisheableCell implements Fisheable {

        private final ImmutableMap<Species, Fisheable> fisheables;

        private FisheableCell(final Int2D cell) {
            fisheables =
                grids
                    .entrySet()
                    .stream()
                    .collect(toImmutableMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getFisheableCell(cell)
                    ));
        }

        @Override
        public Bucket availableFish() {
            return fisheables
                .values()
                .stream()
                .map(Fisheable::availableFish)
                .reduce(Bucket.empty(), Bucket::add);
        }

        @Override
        public void release(final Bucket fishToRelease) {
            fishToRelease.getMap().forEach((species, biomass) -> {
                final Fisheable fisheable = fisheables.get(species);
                if (fisheable == null) throw new IllegalArgumentException(
                    "No grid available to release %s.".formatted(species)
                );
                fisheable.release(Bucket.of(species, biomass));
            });
        }

        @Override
        public Bucket extract(final Bucket fishToExtract) {
            final BucketBuilder fishExtractedSoFar = Bucket.newBuilder();
            final BucketBuilder fishRemainingToExtract = fishToExtract.toBuilder();
            for (final Fisheable fisheable : fisheables.values()) {
                final Bucket fishExtracted =
                    fisheable.extract(fishRemainingToExtract.build());
                fishExtractedSoFar.add(fishExtracted);
                fishRemainingToExtract.subtract(fishExtracted);
            }
            return fishExtractedSoFar.build();
        }
    }
}
