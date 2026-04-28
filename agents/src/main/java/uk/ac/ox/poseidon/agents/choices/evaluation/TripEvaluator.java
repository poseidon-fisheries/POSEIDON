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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.choices.MutableOptionValues;
import uk.ac.ox.poseidon.agents.trips.TripEndEvent;
import uk.ac.ox.poseidon.agents.trips.TripEvent;
import uk.ac.ox.poseidon.agents.trips.TripStartEvent;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.Listener;

@SuppressFBWarnings(value = "EI_EXPOSE_REP2")
public class TripEvaluator implements Listener<TripEvent> {

    private final MutableOptionValues<Int2D> optionValues;
    private final EvaluationProvider<Int2D> evaluationProvider;
    private Evaluation<Int2D> currentEvaluation;

    public TripEvaluator(
        final EventManager eventManager,
        final MutableOptionValues<Int2D> optionValues,
        final EvaluationProvider<Int2D> evaluationProvider
    ) {
        this.optionValues = optionValues;
        this.evaluationProvider = evaluationProvider;
        eventManager.addListener(this);
    }

    @Override
    public Class<? extends TripEvent> getEventClass() {
        return TripEvent.class;
    }

    @Override
    public void receive(final TripEvent event) {
        if (event instanceof TripStartEvent) {
            currentEvaluation =
                evaluationProvider.newEvaluation(
                    event.getTrip().getDestination(),
                    event.getTrip().getEventManager()
                );
        } else if (event instanceof TripEndEvent) {
            optionValues.observe(currentEvaluation.getOption(), currentEvaluation.getResult());
        }
    }
}
