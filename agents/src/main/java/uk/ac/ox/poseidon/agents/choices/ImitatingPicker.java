/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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

import static java.lang.Double.NEGATIVE_INFINITY;
import static lombok.AccessLevel.PACKAGE;
import static uk.ac.ox.poseidon.core.MasonUtils.shuffledStream;

import ec.util.MersenneTwisterFast;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

/**
 * This picker will look for better options than the current option in a list of
 * candidates to imitate and pick one at random if any of them are accessible
 * from the vessel's current location.
 * <p>
 * Note that it is possible for this explorer to return {@code null}, in the
 * case where {@code currentOption} is {@code null} and all the other candidates
 * are also {@code null} (which can happen at the start of a simulation) or
 * inaccessible (which can happen if the vessel is isolated in a lake on a
 * random map). This explorer should therefore never be used as the final option
 * in a chain of explorers; it needs another one to fall back on.
 */
@RequiredArgsConstructor(access = PACKAGE)
public class ImitatingPicker<O> implements Function<Vessel, O> {

  private final Function<
    ? super Vessel,
    ? extends OptionValues<O>
  > optionValues;
  private final BiFunction<? super Vessel, ? super O, Boolean> optionPredicate;
  private final Function<
    ? super Vessel,
    ? extends OptionValues<O>
  > imitatedOptionValues;
  private final MersenneTwisterFast rng;

  /**
   * @param vessel the vessel to pick for
   * @return a uniformly random candidate strictly better than the current
   *         best-known option, if any are accessible; otherwise the current
   *         best-known option; {@code null} if there is none
   */
  @Override
  public O apply(final Vessel vessel) {
    final Optional<Entry<O, Double>> currentBestEntry = optionValues
      .apply(vessel)
      .getBestEntry(rng);

    final double currentBestValue = currentBestEntry
      .map(Entry::getValue)
      .orElse(NEGATIVE_INFINITY);

    final List<O> candidates = new ArrayList<>();
    imitatedOptionValues.apply(vessel).forEachBestEntry((key, value) -> {
      if (value > currentBestValue) candidates.add(key);
    });

    return shuffledStream(candidates, rng)
      .filter(option -> optionPredicate.apply(vessel, option))
      .findFirst()
      .or(() -> currentBestEntry.map(Entry::getKey))
      .filter(option -> optionPredicate.apply(vessel, option))
      .orElse(null);
  }
}
