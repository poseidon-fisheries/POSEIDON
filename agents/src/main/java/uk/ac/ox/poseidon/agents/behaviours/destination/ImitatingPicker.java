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

package uk.ac.ox.poseidon.agents.behaviours.destination;

import ec.util.MersenneTwisterFast;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.behaviours.choices.OptionValues;
import uk.ac.ox.poseidon.agents.behaviours.choices.Picker;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.lang.Double.NEGATIVE_INFINITY;
import static lombok.AccessLevel.PACKAGE;
import static uk.ac.ox.poseidon.core.MasonUtils.shuffledStream;

/**
 * This picker will look for better options than the current option in a list of candidates to
 * imitate and pick one at random if any of them are accessible from the vessel's current location.
 * <p>
 * Note that it is possible for this explorer to return {@code null}, in the case where
 * {@code currentOption} is {@code null} and all the other candidates are also {@code null} (which
 * can happen at the start of a simulation) or inaccessible (which can happen if the vessel is
 * isolated in a lake on a random map). This explorer should therefore never be used as the final
 * option in a chain of explorers; it needs another one to fall back on.
 */
@RequiredArgsConstructor(access = PACKAGE)
public class ImitatingPicker<O> implements Picker<O> {

    private final OptionValues<O> optionValues;
    private final Predicate<O> optionPredicate;
    private final Supplier<OptionValues<O>> candidatesSupplier;
    private final MersenneTwisterFast rng;

    @Override
    public Optional<O> pick() {

        final Optional<Entry<O, Double>> currentBestEntry =
            optionValues.getBestEntry(rng);

        final double currentBestValue =
            currentBestEntry.map(Entry::getValue).orElse(NEGATIVE_INFINITY);

        final List<O> candidates =
            candidatesSupplier
                .get()
                .getBestEntries()
                .stream()
                .filter(entry -> entry.getValue() > currentBestValue)
                .map(Entry::getKey)
                .toList();

        return shuffledStream(candidates, rng)
            .filter(optionPredicate)
            .findFirst()
            .or(() -> currentBestEntry.map(Entry::getKey))
            .filter(optionPredicate);

    }
}
