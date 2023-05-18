/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.actions;

import com.google.common.base.Preconditions;

/**
 * Every action returns the next action to take and a boolean telling whether the action takes place this turn
 * or the next
 */
public class ActionResult {

    private final Action nextState;

    private final double hoursLeft;

    public ActionResult(Action nextState, double hoursLeft) {
        Preconditions.checkArgument(hoursLeft >= 0);
        this.nextState = nextState;
        this.hoursLeft = hoursLeft;
    }

    public Action getNextState() {
        return nextState;
    }

    public double getHoursLeft() {
        return hoursLeft;
    }

    public boolean isActAgainThisTurn() {
        return hoursLeft > 0;
    }


}
