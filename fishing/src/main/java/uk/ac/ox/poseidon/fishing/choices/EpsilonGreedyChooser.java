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

package uk.ac.ox.poseidon.fishing.choices;

import static com.google.common.base.Preconditions.checkNotNull;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;

import ec.util.MersenneTwisterFast;
import java.util.Optional;
import java.util.function.Function;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * Chooses for a vessel: with probability {@code epsilon}, explores (asks the
 * explorer); otherwise exploits (asks the exploiter), falling back to exploring
 * if the exploiter has nothing to offer.
 */
public class EpsilonGreedyChooser<O> implements Function<Vessel, Optional<O>> {

  private final double epsilon;
  private final Function<? super Vessel, ? extends O> explorer;
  private final Function<? super Vessel, ? extends O> exploiter;
  private final MersenneTwisterFast rng;

  /**
   * @param epsilon   probability of exploring instead of exploiting, in
   *                  {@code [0, 1]}
   * @param explorer  gives an exploratory option for a vessel
   * @param exploiter gives the currently-best-known option for a vessel
   * @param rng       the RNG to draw the explore/exploit decision from
   */
  EpsilonGreedyChooser(
    final double epsilon,
    final Function<? super Vessel, ? extends O> explorer,
    final Function<? super Vessel, ? extends O> exploiter,
    final MersenneTwisterFast rng
  ) {
    this.epsilon = checkUnitRange(epsilon, "epsilon");
    this.explorer = checkNotNull(explorer);
    this.exploiter = checkNotNull(exploiter);
    this.rng = checkNotNull(rng);
  }

  /**
   * @param vessel the vessel to choose for
   * @return the exploiter's pick with probability {@code 1 - epsilon} (else the
   *         explorer's), falling back to the explorer if the exploiter returns
   *         {@code null}
   */
  @Override
  public Optional<O> apply(final Vessel vessel) {
    final boolean exploit = !rng.nextBoolean(epsilon);
    O result = null;
    if (exploit) {
      result = this.exploiter.apply(vessel);
    }
    if (result == null) {
      result = this.explorer.apply(vessel);
    }
    return Optional.ofNullable(result);
  }
}
