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

import static lombok.AccessLevel.PACKAGE;

import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * Values an option net of its cost: a revenue valuation minus a cost valuation,
 * both in the context of the same vessel and given the same option and
 * recollection (in NW Med, the value of the remembered catch minus the cost of
 * travelling to fish there). The two are assumed to give amounts in the same
 * currency; this is not checked, since valuations give bare amounts.
 *
 * @param <O> the type of option valued
 * @param <M> the type of what is remembered about an option
 */
@RequiredArgsConstructor(access = PACKAGE)
public class NetValuation<O, M> implements Valuation<O, M> {

  private final Valuation<? super O, ? super M> revenueValuation;
  private final Valuation<? super O, ? super M> costValuation;

  /**
   * @return the revenue valuation of an option minus its cost valuation, each
   *         put in the context of {@code vessel}
   */
  @Override
  public ContextualisedValuation<O, M> inContextOf(final Vessel vessel) {
    final ContextualisedValuation<? super O, ? super M> revenue =
      revenueValuation.inContextOf(vessel);
    final ContextualisedValuation<? super O, ? super M> cost =
      costValuation.inContextOf(vessel);
    return (option, recollection) ->
      revenue.valueOf(option, recollection) -
      cost.valueOf(option, recollection);
  }
}
