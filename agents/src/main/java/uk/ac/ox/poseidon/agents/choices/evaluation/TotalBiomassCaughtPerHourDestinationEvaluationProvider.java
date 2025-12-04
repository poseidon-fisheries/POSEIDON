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

import lombok.Getter;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.tasks.fishing.FishingEvent;
import uk.ac.ox.poseidon.core.events.CombiningEphemeralAccumulatingListener;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.ExtendedEvent;

public class TotalBiomassCaughtPerHourDestinationEvaluationProvider implements EvaluationProvider<Int2D> {

    @Override
    public uk.ac.ox.poseidon.agents.choices.evaluation.Evaluation<Int2D> newEvaluation(
        final Int2D option,
        final EventManager eventManager
    ) {
        return new Evaluation(option, eventManager);
    }

    private static class Evaluation
        implements uk.ac.ox.poseidon.agents.choices.evaluation.Evaluation<Int2D> {

        @Getter private final Int2D option;
        private Double result;

        private CombiningEphemeralAccumulatingListener
            <FishingEvent, Double, ExtendedEvent, Double, Double> listener;

        private Evaluation(
            final Int2D option,
            final EventManager eventManager
        ) {
            this.option = option;
            this.listener = new CombiningEphemeralAccumulatingListener<>(
                eventManager,
                FishingEvent.class,
                0.0,
                (caughtSoFar, fishingEvent) -> caughtSoFar +
                    fishingEvent
                        .getOutcome()
                        .getDisposition()
                        .getRetained()
                        .getTotalBiomass()
                        .asKg(),
                ExtendedEvent.class,
                0.0,
                (hoursSoFar, event) ->
                    hoursSoFar + event.getDuration().toHours(),
                (caught, hours) -> caught / hours
            );
        }

        @Override
        public double getResult() {
            if (result == null) {
                result = listener.get();
                listener = null;
            }
            return result;
        }
    }

}
