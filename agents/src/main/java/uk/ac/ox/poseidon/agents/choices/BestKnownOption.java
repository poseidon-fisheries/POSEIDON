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

import static lombok.AccessLevel.PACKAGE;

import ec.util.MersenneTwisterFast;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

/**
 * Gives, for a vessel, the option with the highest value, ties broken at
 * random, read anew from the vessel's option values on each call; when no
 * option is known, gives what the fallback gives for the vessel (which may be
 * {@code null}).
 *
 * @param <O> the type of option
 */
@RequiredArgsConstructor(access = PACKAGE)
public class BestKnownOption<O> implements Function<Vessel, O> {

  private final @NonNull Function<
    ? super Vessel,
    ? extends OptionValues<O>
  > optionValues;
  private final @NonNull Function<? super Vessel, ? extends O> fallback;
  private final @NonNull MersenneTwisterFast rng;

  @Override
  public O apply(final Vessel vessel) {
    return optionValues
      .apply(vessel)
      .getBestOption(rng)
      .orElseGet(() -> fallback.apply(vessel));
  }
}
