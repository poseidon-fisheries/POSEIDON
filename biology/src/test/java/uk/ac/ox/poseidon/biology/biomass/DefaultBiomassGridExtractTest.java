/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class DefaultBiomassGridExtractTest {

    private static final double EPSILON = 1e-9;

    private static final Int2D CELL = new Int2D(0, 0);
    private static final Species SPECIES = new Species("COD", null, "Cod");
    private static final Species OTHER_SPECIES = new Species("HAD", null, "Haddock");

    @Test
    void extract_returnsRequestedBiomassWhenAvailable() {
        final DefaultBiomassGrid grid = newGrid(10.0);
        final Fisheable fisheable = grid.getFisheableCell(CELL);
        final Bucket request = Bucket.of(Map.of(SPECIES, Biomass.ofKg(7.0)));

        final Bucket extracted = fisheable.extract(request);

        assertThat(extracted.getKg(SPECIES)).isCloseTo(7.0, offset(EPSILON));
        assertThat(grid.getBiomass(CELL).asKg()).isCloseTo(3.0, offset(EPSILON));
    }

    @Test
    void extract_returnsAvailableBiomassWhenRequestExceedsStock() {
        final DefaultBiomassGrid grid = newGrid(5.0);
        final Fisheable fisheable = grid.getFisheableCell(CELL);
        final Bucket request = Bucket.of(Map.of(SPECIES, Biomass.ofKg(9.0)));

        final Bucket extracted = fisheable.extract(request);

        assertThat(extracted.getKg(SPECIES)).isCloseTo(5.0, offset(EPSILON));
        assertThat(grid.getBiomass(CELL).asKg()).isCloseTo(0.0, offset(EPSILON));
    }

    @Test
    void multiSpeciesAvailableFishTreatsNaNAsAbsent() {
        final DefaultBiomassGrid nanGrid = newGrid(SPECIES, Double.NaN);
        final DefaultBiomassGrid biomassGrid = newGrid(OTHER_SPECIES, 5.0);
        final Fisheable fisheable = new FisheableBiomassGrids(List.of(nanGrid, biomassGrid))
            .getFisheableCell(CELL);

        final Bucket available = fisheable.availableFish();

        assertThat(available.getContent(SPECIES)).isEmpty();
        assertThat(available.getKg(OTHER_SPECIES)).isCloseTo(5.0, offset(EPSILON));
        assertThat(available.getSpecies()).isEqualTo(Set.of(OTHER_SPECIES));
    }

    @Test
    void extractEmptyRequestReturnsBucketEmpty() {
        final DefaultBiomassGrid grid = newGrid(10.0);
        final Fisheable fisheable = grid.getFisheableCell(CELL);

        final Bucket extracted = fisheable.extract(Bucket.empty());

        assertThat(extracted).isSameAs(Bucket.empty());
        assertThat(grid.getBiomass(CELL).asKg()).isCloseTo(10.0, offset(EPSILON));
    }

    @Test
    void multiSpeciesExtractFromNaNCellReturnsEmptyBucket() {
        final DefaultBiomassGrid nanGrid = newGrid(SPECIES, Double.NaN);
        final DefaultBiomassGrid emptyGrid = newGrid(OTHER_SPECIES, 0.0);
        final Fisheable fisheable = new FisheableBiomassGrids(List.of(nanGrid, emptyGrid))
            .getFisheableCell(CELL);
        final Bucket request = Bucket.of(Map.of(SPECIES, Biomass.ofKg(5.0)));

        final Bucket extracted = fisheable.extract(request);

        assertThat(extracted).isSameAs(Bucket.empty());
        assertThat(Double.isNaN(nanGrid.getValue(CELL))).isTrue();
    }

    private DefaultBiomassGrid newGrid(final double initialValue) {
        return newGrid(SPECIES, initialValue);
    }

    private DefaultBiomassGrid newGrid(
        final Species species,
        final double initialValue
    ) {
        return new DefaultBiomassGrid(
            ModelGrid.create(1, 1, new Envelope(0, 1, 0, 1)),
            species,
            initialValue
        );
    }
}
