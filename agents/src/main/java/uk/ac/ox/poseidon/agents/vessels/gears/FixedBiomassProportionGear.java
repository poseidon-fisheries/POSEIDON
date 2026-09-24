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

package uk.ac.ox.poseidon.agents.vessels.gears;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.biology.buckets.Bucket;

import java.time.Duration;
import java.util.function.Supplier;

import static uk.ac.ox.poseidon.core.utils.Preconditions.checkPositive;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;

/**
 * A {@link Gear} that catches a fixed proportion of each species' available biomass, per
 * fishing event, discarding any per-species catch below {@code minimumCatchThresholdInKg}.
 */
@Getter
@ToString
public class FixedBiomassProportionGear implements Gear {

    private final String code;
    private final double proportion;
    private final double minimumCatchThresholdInKg;
    private final Supplier<Duration> durationSupplier;
    private final double litresOfFuelConsumedPerHourOfFishing;
    @Setter private boolean active = true;

    /**
     * @param code                                  this gear's identifying code
     * @param proportion                             the fraction of available biomass caught
     *                                               per species, in {@code [0, 1]}
     * @param minimumCatchThresholdInKg              per-species catch below this, in kilograms,
     *                                               is discarded down to zero
     * @param durationSupplier                       supplies how long one fishing event takes
     * @param litresOfFuelConsumedPerHourOfFishing    fuel burned per hour of fishing, in litres
     */
    FixedBiomassProportionGear(
        final String code,
        final double proportion,
        final double minimumCatchThresholdInKg,
        final Supplier<Duration> durationSupplier,
        final double litresOfFuelConsumedPerHourOfFishing
    ) {
        this.code = code;
        this.proportion = checkUnitRange(proportion, "proportion");
        this.minimumCatchThresholdInKg = checkPositive(
            minimumCatchThresholdInKg,
            "minimumCatchThresholdInKg"
        );
        this.durationSupplier = durationSupplier;
        this.litresOfFuelConsumedPerHourOfFishing = litresOfFuelConsumedPerHourOfFishing;
    }

    /**
     * @return {@link #proportion} of each species' available biomass in {@code fisheable},
     * zeroed out per species below {@link #minimumCatchThresholdInKg}, removed from
     * {@code fisheable} in the process
     */
    @Override
    public Bucket fish(final Fisheable fisheable) {
        final Bucket fishToCatch =
            fisheable
                .availableFish()
                .mapBiomassValue((_, biomass) -> {
                    final double v = biomass * proportion;
                    return v >= minimumCatchThresholdInKg ? v : 0;
                });
        final Bucket fishExtracted =
            fisheable.extract(fishToCatch);
        assert fishExtracted.equals(fishToCatch);
        return fishExtracted;
    }
}
