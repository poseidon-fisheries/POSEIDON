/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.fishing.choices.MutableOptionValues;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScopeFactory;

/**
 * A {@link VesselComponentScopeFactory} counterpart of {@link TripEvaluator}, built via
 * {@link Factories#tripEvaluator}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class TripEvaluatorFactory
  extends VesselComponentScopeFactory<TripEvaluator>
{

  private final Factory<
    ? super VesselComponentScope,
    ? extends Function<? super Vessel, ? extends MutableOptionValues<Int2D>>
  > optionValues;
  private final Factory<
    ? super VesselComponentScope,
    ? extends Evaluator<Int2D>
  > evaluationProvider;

  @Override
  protected TripEvaluator newInstance(final VesselComponentScope scope) {
    final TripEvaluator tripEvaluator = new TripEvaluator(
      optionValues.get(scope),
      evaluationProvider.get(scope)
    );
    scope.addListener(tripEvaluator);
    return tripEvaluator;
  }
}
