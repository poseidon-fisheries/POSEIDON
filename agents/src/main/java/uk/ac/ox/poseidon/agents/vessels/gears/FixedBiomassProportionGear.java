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

import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.biology.biomass.Biomass;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkArgument;

public class FixedBiomassProportionGear implements FishingGear<Biomass> {

    private final double proportion;
    private final Duration duration;

    public FixedBiomassProportionGear(
        final double proportion,
        final Duration duration
    ) {
        checkArgument(proportion >= 0 && proportion <= 1);
        this.proportion = proportion;
        this.duration = duration;
    }

    @Override
    public Duration nextDuration() {
        return duration;
    }

    @Override
    public Bucket<Biomass> fish(final Fisheable<Biomass> fisheable) {
        final Bucket<Biomass> fishToCatch =
            fisheable
                .availableFish()
                .mapContent(biomass ->
                    Biomass.of(biomass.getValue() * proportion)
                );
        final Bucket<Biomass> fishExtracted =
            fisheable.extract(fishToCatch);
        assert fishExtracted.equals(fishToCatch);
        return fishExtracted;
    }
}
