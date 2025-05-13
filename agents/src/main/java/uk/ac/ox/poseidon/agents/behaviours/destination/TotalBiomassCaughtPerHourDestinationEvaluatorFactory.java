/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.agents.behaviours.destination;

import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.behaviours.Action;
import uk.ac.ox.poseidon.agents.behaviours.choices.Evaluator;
import uk.ac.ox.poseidon.agents.behaviours.fishing.FishingAction;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.events.CombiningEphemeralAccumulatingListener;

public class TotalBiomassCaughtPerHourDestinationEvaluatorFactory
    extends VesselScopeFactory<Evaluator<Int2D>> {

    @Override
    protected Evaluator<Int2D> newInstance(
        final Simulation simulation,
        final Vessel vessel
    ) {
        return option -> new Evaluation(vessel);
    }

    private static class Evaluation implements uk.ac.ox.poseidon.agents.behaviours.choices.Evaluation {

        private final CombiningEphemeralAccumulatingListener
            <FishingAction, Double, Action, Double, Double> listener;

        private Evaluation(final Vessel vessel) {
            listener = new CombiningEphemeralAccumulatingListener<>(
                vessel.getEventManager(),
                FishingAction.class,
                0.0,
                (caughtSoFar, fishingAction) -> caughtSoFar +
                    fishingAction.getDisposition().getRetained().getTotalBiomass().asKg(),
                Action.class,
                0.0,
                (hoursSoFar, action) ->
                    vessel.isAtPort()
                        ? hoursSoFar
                        : hoursSoFar + action.getDuration().toHours(),
                (caught, hours) -> caught / hours
            );
        }

        @Override
        public double getResult() {
            return listener.get();
        }
    }
}
