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
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.function.Consumer;

public abstract class YearlyActionLimitRegulation implements ActionSpecificRegulation {

    private static final long serialVersionUID = 5818383237466189702L;
    private final FisherRelativeLimits limits;
    private int counter = 0;

    YearlyActionLimitRegulation(
        final Consumer<Startable> startableConsumer,
        final FisherRelativeLimits limits
    ) {
        this.limits = limits;
        startableConsumer.accept(this);
    }

    @Override
    public boolean isForbidden(final Class<? extends PurseSeinerAction> action, final Fisher fisher) {
        assert getApplicableActions().contains(action);
        return counter >= getLimit(fisher);
    }

    public int getLimit(final Fisher fisher) {
        return limits.getLimit(fisher);
    }

    public double getPctLimitRemaining(final Fisher fisher) {
        final int limit = getLimit(fisher);
        return (double) (limit - counter) / limit;
    }

    @Override
    public void step(final SimState simState) {
        counter = 0;
    }

    @Override
    public void start(final FishState model) {
        model.scheduleEveryYear(this, StepOrder.DAWN);
    }

    public int getNumRemainingActions(final Fisher fisher) {
        return getLimit(fisher) - counter;
    }

    @Override
    public void observe(final PurseSeinerAction action) {
        assert getApplicableActions().contains(action.getClass());
        counter++;
    }

}
