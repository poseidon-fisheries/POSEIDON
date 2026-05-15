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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sim.field.grid.DoubleGrid2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.biology.FisheableGrid;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.buckets.SingleSpeciesBiomassBucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.grids.MutableDoubleGrid;

@Getter
class DefaultBiomassGrid extends MutableDoubleGrid implements BiomassGrid, FisheableGrid {

    private final Species species;

    public DefaultBiomassGrid(
        final ModelGrid modelGrid,
        final Species species
    ) {
        super(modelGrid);
        this.species = species;
    }

    public DefaultBiomassGrid(
        final ModelGrid modelGrid,
        final Species species,
        final double initialValue
    ) {
        super(modelGrid, initialValue);
        this.species = species;
    }

    public DefaultBiomassGrid(
        final ModelGrid modelGrid,
        final Species species,
        final double[][] values
    ) {
        super(modelGrid, values);
        this.species = species;
    }

    public DefaultBiomassGrid(
        final ModelGrid modelGrid,
        final Species species,
        final DoubleGrid2D grid
    ) {
        super(modelGrid, grid);
        this.species = species;
    }

    @Override
    public Biomass getBiomass(final Int2D cell) {
        return Biomass.ofKg(getValue(cell));
    }

    @Override
    public void setBiomass(
        final Int2D cell,
        final double value
    ) {
        setValue(cell, value);
    }

    @Override
    public Fisheable getFisheableCell(final Int2D cell) {
        return new FisheableCell(cell);
    }

    /**
     * Sums the values for the grid, ignoring NaN, as they occur in non-habitable cells
     */
    @Override
    public double getSum() {
        double sum = 0.0;
        final int width = field.width;
        final int height = field.height;
        final double[][] a = field.field;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                final double biomass = a[x][y];
                if (!Double.isNaN(biomass)) {
                    sum += biomass;
                }
            }
        }
        return sum;
    }

    @RequiredArgsConstructor
    class FisheableCell implements Fisheable {

        final Int2D cell;

        @Override
        public Bucket availableFish() {
            return Bucket.of(species, getValue(cell));
        }

        @Override
        public void release(final Bucket fishToRelease) {
            fishToRelease.forEachBiomassValue((s, biomass) -> {
                if (s.equals(species))
                    setBiomass(cell, getBiomass(cell).asKg() + biomass);
                else {
                    throw new IllegalArgumentException(
                        "Unable to release %s in a %s grid".formatted(s, species)
                    );
                }
            });
        }

        @Override
        public Bucket extract(final Bucket fishToExtract) {
            final double biomassToExtract = fishToExtract.getKg(species);
            if (biomassToExtract <= 0) return Bucket.empty();
            final double gridBiomass = getValue(cell);
            if (gridBiomass <= 0) return Bucket.empty();
            final double biomassExtracted = Math.min(biomassToExtract, gridBiomass);
            setValue(cell, gridBiomass - biomassExtracted);
            return new SingleSpeciesBiomassBucket(species, biomassExtracted);
        }
    }
}
