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

package uk.ac.ox.poseidon.fishing.choices.evaluation;

import java.util.function.Function;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.fishing.choices.MutableOptionValues;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.geography.grids.Cell;

/**
 * Factories for scoring a vessel's destination choices and feeding the scores
 * back in.
 */
public final class Factories {

  private Factories() {}

  /**
   * @param currencyCode the currency code trip profit is measured in
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link ProfitPerHourDestinationEvaluator}
   * @see ProfitPerHourDestinationEvaluator
   */
  public static ProfitPerHourDestinationEvaluationProviderFactory profitPerHour(
    final String currencyCode
  ) {
    return new ProfitPerHourDestinationEvaluationProviderFactory(currencyCode);
  }

  /**
   * @param optionValues       gives the option values of a vessel, where each
   *                           of its completed trips' (destination, score)
   *                           observation is fed: the option values'
   *                           component, say
   * @param evaluationProvider starts a fresh evaluation for each trip's
   *                           destination
   * @return a {@link uk.ac.ox.poseidon.fishing.vessels.VesselComponentScopeFactory}
   *         for a {@link TripEvaluator}, registered on the vessel's event
   *         manager
   * @see TripEvaluator
   */
  public static TripEvaluatorFactory tripEvaluator(
    final Factory<
      ? super VesselComponentScope,
      ? extends Function<? super Vessel, ? extends MutableOptionValues<Cell>>
    > optionValues,
    final Factory<
      ? super VesselComponentScope,
      ? extends Evaluator<Cell>
    > evaluationProvider
  ) {
    return new TripEvaluatorFactory(optionValues, evaluationProvider);
  }
}
