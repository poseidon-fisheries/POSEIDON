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

import static com.google.common.base.Preconditions.checkState;

import java.util.function.Function;
import uk.ac.ox.poseidon.core.events.Listener;
import uk.ac.ox.poseidon.fishing.choices.MutableOptionValues;
import uk.ac.ox.poseidon.fishing.trips.TripEndEvent;
import uk.ac.ox.poseidon.fishing.trips.TripEvent;
import uk.ac.ox.poseidon.fishing.trips.TripStartEvent;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.geography.grids.Cell;

/**
 * Listens for a vessel's trip start/end events and feeds each completed trip's
 * destination and score into the option values of the trip's vessel, read
 * when the trip ends, so future destination choices can learn from past
 * outcomes. Its factory registers it on the vessel's event manager.
 */
public class TripEvaluator implements Listener<TripEvent> {

  private final Function<
    ? super Vessel,
    ? extends MutableOptionValues<Cell>
  > optionValues;
  private final Evaluator<Cell> evaluator;
  private Evaluation<Cell> currentEvaluation;

  /**
   * @param optionValues gives the option values of a vessel, where each of its
   *                     completed trips' (destination, score) observation is
   *                     fed
   * @param evaluator    starts a fresh {@link Evaluation} for each trip's
   *                     destination
   */
  public TripEvaluator(
    final Function<
      ? super Vessel,
      ? extends MutableOptionValues<Cell>
    > optionValues,
    final Evaluator<Cell> evaluator
  ) {
    this.optionValues = optionValues;
    this.evaluator = evaluator;
  }

  /** @return {@link TripEvent}{@code .class} */
  @Override
  public Class<? extends TripEvent> getEventClass() {
    return TripEvent.class;
  }

  /**
   * On a {@link TripStartEvent}, starts a fresh evaluation of the trip's
   * destination. On a {@link TripEndEvent}, feeds that evaluation's (option,
   * result) pair into the option values of the trip's vessel.
   *
   * @throws IllegalStateException if a trip starts headed for a destination
   *                               that is not a cell: the options learnt from
   *                               are fishing grounds
   */
  @Override
  public void receive(final TripEvent event) {
    if (event instanceof TripStartEvent) {
      checkState(
        event.getTrip().getDestination() instanceof Cell,
        "The trip of vessel %s starts headed for %s, which is not a cell.",
        event.getTrip().getVessel(),
        event.getTrip().getDestination()
      );
      currentEvaluation = evaluator.newEvaluation(
        (Cell) event.getTrip().getDestination(),
        event.getTrip().getEventManager()
      );
    } else if (event instanceof TripEndEvent) {
      optionValues
        .apply(event.getTrip().getVessel())
        .observe(currentEvaluation.getOption(), currentEvaluation.getResult());
    }
  }
}
