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

package uk.ac.ox.poseidon.fishing.choices;

import static uk.ac.ox.poseidon.core.MasonUtils.shuffledStream;

import ec.util.MersenneTwisterFast;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * Picks uniformly among a vessel's eligible candidates, or gives no option.
 * The candidate list is read once per pick, must have a stable order and must
 * contain distinct options. These requirements are documented, not checked.
 * @param <O> the option type
 */
@RequiredArgsConstructor
public class RandomPicker<O> implements Function<Vessel, Optional<O>> {

  private final @NonNull Function<
    ? super Vessel,
    ? extends List<? extends O>
  > candidates;
  private final @NonNull BiFunction<
    ? super Vessel,
    ? super O,
    Boolean
  > optionEligibilityCriteria;
  private final @NonNull MersenneTwisterFast rng;

  @Override
  public Optional<O> apply(final Vessel vessel) {
    return pickFrom(
      vessel,
      candidates.apply(vessel),
      optionEligibilityCriteria,
      rng
    );
  }

  /** Uniform eligible draw; eligibility is tested lazily in random order. */
  static <O> Optional<O> pickFrom(
    final Vessel vessel,
    final List<? extends O> candidates,
    final BiFunction<
      ? super Vessel,
      ? super O,
      Boolean
    > optionEligibilityCriteria,
    final MersenneTwisterFast rng
  ) {
    return shuffledStream(candidates, rng)
      .filter(option -> optionEligibilityCriteria.apply(vessel, option))
      .findFirst()
      .map(option -> option);
  }
}
