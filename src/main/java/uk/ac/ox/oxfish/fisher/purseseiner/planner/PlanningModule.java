/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import org.jetbrains.annotations.Nullable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;

/**
 * an interface describing a component for the a planner: it is given a current path, it must both (a) generate new actions/candidates
 * (b) choose among them only one to return
 *
 */
public interface PlanningModule extends FisherStartable {


    @Nullable
    public PlannedAction chooseNextAction(Plan currentPlanSoFar);

    public boolean isStarted();

    /**
     * this is like the start(...) but gets called when we want the module to be aware that a new plan is starting
     * @param state
     * @param fisher
     */
    public void prepareForReplanning(FishState state, Fisher fisher);

    /**
     * if a plan is about to start, how many times are we allowed to call this planning module (it may fail before then, the
     * point of this function is to deal with regulations or other constraints)
     * @param state
     * @param fisher
     * @return
     */
    public int maximumActionsInAPlan(FishState state, Fisher fisher);

}
