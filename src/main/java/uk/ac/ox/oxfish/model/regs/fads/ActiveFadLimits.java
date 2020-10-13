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

import com.google.common.collect.ImmutableSet;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadDeploymentAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManagerUtils.getFadManager;

public class ActiveFadLimits implements ActionSpecificRegulation {

    private final ImmutableSet<Class<? extends PurseSeinerAction>> applicableActions =
        ImmutableSet.of(FadDeploymentAction.class);
    private final FisherRelativeLimits limits;

    public ActiveFadLimits(Iterable<? extends ConditionalLimit> limits) {
        this(new ConditionalFisherRelativeLimits(limits));
    }

    private ActiveFadLimits(FisherRelativeLimits limits) {
        this.limits = limits;
    }

    @Override
    public ImmutableSet<Class<? extends PurseSeinerAction>> getApplicableActions() { return applicableActions; }

    @Override public boolean isForbidden(PurseSeinerAction action) {
        assert applicableActions.contains(action.getClass());
        return getFadManager(action.getFisher()).getNumDeployedFads() >= getLimit(action.getFisher());
    }

    public int getLimit(final Fisher fisher) { return limits.getLimit(fisher); }

}
