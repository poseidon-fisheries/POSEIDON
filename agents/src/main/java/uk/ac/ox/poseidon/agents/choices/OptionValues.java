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

package uk.ac.ox.poseidon.agents.choices;

import ec.util.MersenneTwisterFast;
import it.unimi.dsi.fastutil.objects.ObjectDoubleBiConsumer;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * A learned value per option (e.g. per destination cell), supporting lookup of the current best
 * option(s) — the highest-valued one(s), possibly tied.
 *
 * @param <O> the type of option valued
 */
public interface OptionValues<O> {

    /** @return {@code option}'s current value, if it has been observed */
    Optional<Double> getValue(O option);

    /** @return every option currently tied for the highest value */
    List<O> getBestOptions();

    /** @param rng the RNG to break ties with; @return one option uniformly picked among the best, if any */
    Optional<O> getBestOption(MersenneTwisterFast rng);

    /** @return the current highest value, if any options have been observed */
    Optional<Double> getBestValue();

    /** @return every (option, value) pair currently tied for the highest value */
    List<Entry<O, Double>> getBestEntries();

    /** @param rng the RNG to break ties with; @return one (option, value) pair uniformly picked among the best, if any */
    Optional<Entry<O, Double>> getBestEntry(MersenneTwisterFast rng);

    /**
     * Calls {@code consumer} once per observed option, with its value, in no particular order.
     *
     * @param consumer the consumer to call
     */
    void forEachEntry(ObjectDoubleBiConsumer<? super O> consumer);

    /** Calls {@code consumer} once per {@link #getBestEntries()} entry. */
    default void forEachBestEntry(final ObjectDoubleBiConsumer<? super O> consumer) {
        getBestEntries().forEach(entry -> consumer.accept(entry.getKey(), entry.getValue()));
    }
}
