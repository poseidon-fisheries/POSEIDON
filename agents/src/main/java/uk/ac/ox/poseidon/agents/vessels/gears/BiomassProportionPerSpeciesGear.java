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

package uk.ac.ox.poseidon.agents.vessels.gears;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.biology.buckets.BiomassBucket;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.SpeciesIndex;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;

@Getter
@ToString
public class BiomassProportionPerSpeciesGear implements Gear {

    @NonNull private final String code;

    @NonNull private final SpeciesIndex speciesIndex;
    private final double[] proportions;

    @NonNull private final Supplier<Duration> durationSupplier;
    @Setter private boolean active = true;

    public BiomassProportionPerSpeciesGear(
        @NonNull final String code,
        @NonNull final SpeciesIndex speciesIndex,
        final double[] proportions,
        @NonNull final Supplier<Duration> durationSupplier
    ) {
        checkArgument(proportions.length == speciesIndex.size());
        checkArgument(Arrays.stream(proportions).allMatch(p -> p >= 0 && p <= 1));
        this.code = code;
        this.speciesIndex = speciesIndex;
        this.proportions = proportions.clone();
        this.durationSupplier = durationSupplier;
    }

    @Override
    public Bucket fish(final Fisheable fisheable) {
        final Bucket fishToCatch =
            switch (fisheable.availableFish()) {
                case final BiomassBucket availableFish
                    when availableFish.getSpeciesIndex().equals(speciesIndex) ->
                    availableFish.mapBiomassValueWithIndex((biomass, index) ->
                        proportions[index] * biomass
                    );
                case final Bucket availableFish ->
                    availableFish.mapBiomassValue((species, biomass) -> {
                        final int i = speciesIndex.indexOf(species);
                        return i == -1 ? 0 : proportions[i] * biomass;
                    });
            };
        final Bucket fishExtracted = fisheable.extract(fishToCatch);
        assert fishExtracted.equals(fishToCatch);
        return fishExtracted;
    }
}
