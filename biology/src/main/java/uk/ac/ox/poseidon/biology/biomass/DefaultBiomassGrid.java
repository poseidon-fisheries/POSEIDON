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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sim.field.grid.DoubleGrid2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.biology.FisheableGrid;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.grids.MutableDoubleGrid;

@Getter
class DefaultBiomassGrid extends MutableDoubleGrid implements BiomassGrid, FisheableGrid<Biomass> {

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
        return Biomass.ofKg(getDouble(cell));
    }

    @Override
    public void setBiomass(
        final Int2D cell,
        final double value
    ) {
        setValue(cell, value);
    }

    @Override
    public Fisheable<Biomass> getFisheableCell(final Int2D cell) {
        return new FisheableCell(cell);
    }

    @RequiredArgsConstructor
    class FisheableCell implements Fisheable<Biomass> {

        final Int2D cell;

        @Override
        public Bucket<Biomass> availableFish() {
            return Bucket.of(ImmutableMap.of(species, getBiomass(cell)));
        }

        @Override
        public void release(final Bucket<Biomass> fishToRelease) {
            fishToRelease.getMap().forEach((s, biomass) -> {
                if (s.equals(species))
                    setBiomass(cell, getBiomass(cell).asKg() + biomass.asKg());
                else {
                    throw new IllegalArgumentException(
                        "Unable to release %s in a %s grid".formatted(s, species)
                    );
                }
            });
        }

        @Override
        public Bucket<Biomass> extract(final Bucket<Biomass> fishToExtract) {
            final Bucket.Builder<Biomass> fishExtracted = Bucket.newBuilder();
            fishToExtract
                .getContent(species)
                .map(Biomass::asKg)
                .ifPresent(biomassToExtract -> {
                    final double gridBiomass = getBiomass(cell).asKg();
                    final double biomassExtracted = Math.min(biomassToExtract, gridBiomass);
                    setBiomass(cell, gridBiomass - biomassExtracted);
                    fishExtracted.add(species, Biomass.ofKg(biomassExtracted));
                });
            return fishExtracted.build();
        }
    }
}
