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

package uk.ac.ox.poseidon.agents.choices;

import uk.ac.ox.poseidon.biology.buckets.Bucket;

import java.util.function.BinaryOperator;

import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;

/**
 * A {@link Memory} update rule for {@link Bucket}s: an exponential moving average taken species by
 * species, which remembers {@code recollection × (1 − alpha) + observation × alpha} of each
 * species' biomass. A species missing from either bucket counts as zero there, so a species that is
 * remembered but not observed decays, and one observed for the first time enters at
 * {@code alpha} of its observed biomass.
 * <p>
 * Every species' content is averaged as {@link uk.ac.ox.poseidon.biology.biomass.Biomass}, so any
 * structure a content has beyond its weight in kilograms is lost.
 */
public class ExponentialMovingAverageOfBuckets implements BinaryOperator<Bucket> {

    private final double alpha;

    /** @param alpha the weight of a new observation, between 0 and 1 */
    ExponentialMovingAverageOfBuckets(final double alpha) {
        this.alpha = checkUnitRange(alpha, "alpha");
    }

    @Override
    public Bucket apply(
        final Bucket recollection,
        final Bucket observation
    ) {
        return recollection
            .mapBiomassValue((species, kg) -> kg * (1 - alpha))
            .add(observation.mapBiomassValue((species, kg) -> kg * alpha));
    }
}
