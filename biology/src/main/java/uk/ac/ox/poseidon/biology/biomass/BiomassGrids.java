/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.biology.biomass;

import com.google.common.collect.ImmutableMap;
import sim.util.Int2D;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.biology.FisheableGrid;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

public class BiomassGrids implements FisheableGrid<Biomass> {

    private final Map<Species, BiomassGrid> grids;

    public BiomassGrids(final Collection<BiomassGrid> grids) {
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
    public Fisheable<Biomass> getFisheableCell(final Int2D cell) {
        return new FisheableCell(cell);
    }

    class FisheableCell implements Fisheable<Biomass> {

        private final List<Fisheable<Biomass>> fisheables;

        private FisheableCell(final Int2D cell) {
            fisheables =
                grids
                    .values()
                    .stream()
                    .map(grid -> grid.getFisheableCell(cell))
                    .collect(toList());
        }

        @Override
        public Bucket<Biomass> availableFish() {
            return fisheables
                .stream()
                .map(Fisheable::availableFish)
                .reduce(Bucket.empty(), Bucket::add);
        }

        @Override
        public Bucket<Biomass> extract(final Bucket<Biomass> fishToExtract) {
            final Bucket.Builder<Biomass> fishExtractedSoFar = Bucket.newBuilder();
            final Bucket.Builder<Biomass> fishRemainingToExtract = fishToExtract.toBuilder();
            for (final Fisheable<Biomass> fisheable : fisheables) {
                final Bucket<Biomass> fishExtracted =
                    fisheable.extract(fishRemainingToExtract.build());
                fishExtractedSoFar.add(fishExtracted);
                fishRemainingToExtract.subtract(fishExtracted);
            }
            return fishExtractedSoFar.build();
        }
    }
}
