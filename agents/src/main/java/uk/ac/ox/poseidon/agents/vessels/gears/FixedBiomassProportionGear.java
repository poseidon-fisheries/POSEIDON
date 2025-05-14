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

package uk.ac.ox.poseidon.agents.vessels.gears;

import lombok.Getter;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.biology.biomass.Biomass;

import java.time.Duration;
import java.util.function.Supplier;

import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;

@Getter
public class FixedBiomassProportionGear implements FishingGear<Biomass> {

    private final double proportion;
    private final Supplier<Duration> durationSupplier;

    FixedBiomassProportionGear(
        final double proportion,
        final Supplier<Duration> durationSupplier
    ) {
        this.proportion = checkUnitRange(proportion, "proportion");
        this.durationSupplier = durationSupplier;
    }

    @Override
    public Bucket<Biomass> fish(final Fisheable<Biomass> fisheable) {
        final Bucket<Biomass> fishToCatch =
            fisheable
                .availableFish()
                .mapContent(biomass ->
                    Biomass.ofKg(biomass.asKg() * proportion)
                );
        final Bucket<Biomass> fishExtracted =
            fisheable.extract(fishToCatch);
        assert fishExtracted.equals(fishToCatch);
        return fishExtracted;
    }
}
