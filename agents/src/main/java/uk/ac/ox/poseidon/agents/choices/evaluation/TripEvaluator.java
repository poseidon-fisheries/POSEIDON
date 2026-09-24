/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.agents.choices.evaluation;

import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.choices.MutableOptionValues;
import uk.ac.ox.poseidon.agents.trips.TripEndEvent;
import uk.ac.ox.poseidon.agents.trips.TripEvent;
import uk.ac.ox.poseidon.agents.trips.TripStartEvent;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.Listener;

/**
 * Listens for a vessel's trip start/end events and feeds each completed trip's destination and
 * score into {@code optionValues}, so future destination choices can learn from past outcomes.
 * Registers itself with {@code eventManager} on construction.
 */
public class TripEvaluator implements Listener<TripEvent> {

    private final MutableOptionValues<Int2D> optionValues;
    private final Evaluator<Int2D> evaluator;
    private Evaluation<Int2D> currentEvaluation;

    /**
     * @param eventManager the event manager to listen on for trip start/end events
     * @param optionValues where each completed trip's (destination, score) observation is fed
     * @param evaluator    starts a fresh {@link Evaluation} for each trip's destination
     */
    public TripEvaluator(
        final EventManager eventManager,
        final MutableOptionValues<Int2D> optionValues,
        final Evaluator<Int2D> evaluator
    ) {
        this.optionValues = optionValues;
        this.evaluator = evaluator;
        eventManager.addListener(this);
    }

    /** @return {@link TripEvent}{@code .class} */
    @Override
    public Class<? extends TripEvent> getEventClass() {
        return TripEvent.class;
    }

    /**
     * On a {@link TripStartEvent}, starts a fresh evaluation of the trip's destination. On a
     * {@link TripEndEvent}, feeds that evaluation's (option, result) pair into
     * {@link #optionValues}.
     */
    @Override
    public void receive(final TripEvent event) {
        if (event instanceof TripStartEvent) {
            currentEvaluation =
                evaluator.newEvaluation(
                    event.getTrip().getDestination(),
                    event.getTrip().getEventManager()
                );
        } else if (event instanceof TripEndEvent) {
            optionValues.observe(currentEvaluation.getOption(), currentEvaluation.getResult());
        }
    }
}
