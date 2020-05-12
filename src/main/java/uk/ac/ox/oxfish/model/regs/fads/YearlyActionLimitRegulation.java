/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.model.regs.fads;

import sim.engine.SimState;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.PurseSeinerAction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.function.Consumer;

public abstract class YearlyActionLimitRegulation implements ActionSpecificRegulation {

    private final FisherRelativeLimits limits;
    private int counter = 0;

    YearlyActionLimitRegulation(
        Consumer<Startable> startableConsumer,
        FisherRelativeLimits limits
    ) {
        this.limits = limits;
        startableConsumer.accept(this);
    }

    @Override public boolean isForbidden(PurseSeinerAction action) {
        assert getApplicableActions().contains(action.getClass());
        return counter >= getLimit(action.getFisher());
    }

    public int getLimit(Fisher fisher) { return limits.getLimit(fisher); }

    public int getNumRemainingActions(Fisher fisher) { return getLimit(fisher) - counter; }

    @Override public void observe(PurseSeinerAction action) {
        assert getApplicableActions().contains(action.getClass());
        counter++;
    }

    @Override public void step(SimState simState) {
        counter = 0;
    }

    @Override public void start(FishState model) {
        model.scheduleEveryYear(this, StepOrder.DAWN);
    }
}
