/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2022-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

/**
 * an interface describing a component for the planner: it is given a current path, it must both (a) generate new
 * actions/candidates (b) choose among them only one to return
 */
public interface PlanningModule extends FisherStartable {

    // The max total number of actions observed in a trip is 658,
    // so 1000 seems reasonable. It is used as an upper bound in
    // the binary search for the maximum number of permissible
    // actions, so we want to keep the number as low as possible.
    int MAX_NUMBER_OF_POSSIBLE_ACTIONS = 1000;

    PlannedAction chooseNextAction(Plan currentPlanSoFar);

    /**
     * this is like the start(...) but gets called when we want the module to be aware that a new plan is starting
     *
     * @param state
     * @param fisher
     */
    void prepareForReplanning(
        FishState state,
        Fisher fisher
    );

    default int numberOfPermittedActions(
        final Fisher fisher,
        final Regulations regulations
    ) {
        return getFadManager(fisher)
            .numberOfPermissibleActions(
                getActionClass(),
                numberOfPossibleActions(fisher),
                regulations
            );
    }

    ActionClass getActionClass();

    default int numberOfPossibleActions(final Fisher fisher) {
        return MAX_NUMBER_OF_POSSIBLE_ACTIONS;
    }

    @Override
    void start(
        FishState model,
        Fisher fisher
    );

    @Override
    void turnOff(Fisher fisher);
}
