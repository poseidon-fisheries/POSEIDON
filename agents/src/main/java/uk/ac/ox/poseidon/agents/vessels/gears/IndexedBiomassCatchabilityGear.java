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
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.SpeciesIndexedDoubleArray;

import java.time.Duration;
import java.util.function.Supplier;

import static uk.ac.ox.poseidon.core.utils.Preconditions.checkPositive;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;

/**
 * A {@link Gear} that catches a per-species proportion of each species' available biomass, per
 * fishing event, discarding any per-species catch below {@code minimumCatchThresholdInKg}.
 * Unlike {@link FixedBiomassProportionGear}, the proportion varies by species.
 */
@Getter
@ToString
public class IndexedBiomassCatchabilityGear implements Gear {

    @NonNull private final String code;

    private final SpeciesIndexedDoubleArray proportions;
    private final double minimumCatchThresholdInKg;

    @NonNull private final Supplier<Duration> durationSupplier;
    @Setter private boolean active = true;

    /**
     * @param code                       this gear's identifying code
     * @param proportions                the fraction of available biomass caught, per species,
     *                                   each in {@code [0, 1]}
     * @param minimumCatchThresholdInKg  per-species catch below this, in kilograms, is discarded
     *                                   down to zero
     * @param durationSupplier           supplies how long one fishing event takes
     */
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

    /**
     * @return each species' {@link #proportions} entry times its available biomass in
     * {@code fisheable}, zeroed out per species below {@link #minimumCatchThresholdInKg},
     * removed from {@code fisheable} in the process
     */
    @Override
    public Bucket fish(final Fisheable fisheable) {
        final Bucket availableFish = fisheable.availableFish();
        final Bucket fishToCatch = availableFish.mapWithIndex(proportions, (biomass, i) -> {
            final double v = proportions.getDouble(i) * biomass;
            return v >= minimumCatchThresholdInKg ? v : 0;
        });
        return fisheable.extract(fishToCatch);
    }

    @Override
    public String getCode() {
        return code;
    }
}
