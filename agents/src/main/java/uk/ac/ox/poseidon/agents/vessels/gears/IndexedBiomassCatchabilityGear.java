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
import uk.ac.ox.poseidon.biology.species.SpeciesIndexedDoubleArray;

import java.time.Duration;
import java.util.function.Supplier;

import static uk.ac.ox.poseidon.core.utils.Preconditions.checkPositive;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;

@Getter
@ToString
public class IndexedBiomassCatchabilityGear implements Gear {

    @NonNull private final String code;

    private final SpeciesIndexedDoubleArray proportions;
    private final double minimumCatchThresholdInKg;

    @NonNull private final Supplier<Duration> durationSupplier;
    @Setter private boolean active = true;

    public IndexedBiomassCatchabilityGear(
        @NonNull final String code,
        @NonNull final SpeciesIndexedDoubleArray proportions,
        final double minimumCatchThresholdInKg,
        @NonNull final Supplier<Duration> durationSupplier
    ) {
        proportions.forEachValue(value -> checkUnitRange(value, "proportion"));
        this.code = code;
        this.proportions = proportions;
        this.minimumCatchThresholdInKg = checkPositive(
            minimumCatchThresholdInKg,
            "minimumCatchThresholdInKg"
        );
        this.durationSupplier = durationSupplier;
    }

    @Override
    public Bucket fish(final Fisheable fisheable) {
        final Bucket availableFish = fisheable.availableFish();
        final Bucket fishToCatch =
            switch (availableFish) {
                case final BiomassBucket biomassBucket when proportions.sameIndex(biomassBucket) ->
                    biomassBucket.mapWithIndex((biomass, i) -> {
                        final double v = proportions.getDouble(i) * biomass;
                        return v >= minimumCatchThresholdInKg ? v : 0;
                    });
                default -> availableFish.mapBiomassValue((species, biomass) -> {
                    final double v = proportions.getDoubleOrDefault(species, 0.0) * biomass;
                    return v >= minimumCatchThresholdInKg ? v : 0;
                });
            };
        return fisheable.extract(fishToCatch);
    }

    @Override
    public String getCode() {
        return code;
    }
}
