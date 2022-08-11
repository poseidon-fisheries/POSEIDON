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

package uk.ac.ox.oxfish.fisher.purseseiner.actions;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultipleRegulations;
import uk.ac.ox.oxfish.model.regs.NoFishing;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.TemporaryRegulation;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class FadDeploymentAction<B extends LocalBiology, F extends AbstractFad<B, F>> extends PurseSeinerAction
    implements FadRelatedAction<B, F> {

    // TODO: this should ideally be configurable, but we'll need to implement
    //       temporary regulation at the action specific level for that.
    private static final int BUFFER_PERIOD_BEFORE_CLOSURE = 15;

    private F fad;

    public FadDeploymentAction(final Fisher fisher) {
        super(
            fisher,
            5.0 / 60 // see https://github.com/poseidon-fisheries/tuna/issues/6
        );
    }

    @Override
    public ActionResult act(
        final FishState fishState,
        final Fisher fisher,
        final Regulation ignored,
        final double hoursLeft
    ) {
        assert (fisher == getFisher());
        assert (fisher.getLocation() == getLocation());
        @SuppressWarnings("unchecked")
        final FadManager<B, F> fadManager = (FadManager<B, F>) getFadManager(fisher);
        this.fad = fadManager.deployFad(getLocation(), fishState.random);
        setTime(hoursLeft);
        fadManager.reactTo(this);
        return new ActionResult(new Arriving(), hoursLeft - getDuration());
    }

    /**
     * This little piece of ugliness is my "solution" to the problem of disallowing FAD deployments 15 days before
     * the start of a temporary closure. It recursively digs down the regulation hierarchy to see if a NoFishing
     * regulation will be active at the specified step. It currently assumes that the regulation is some combination
     * of MultipleRegulations and TemporaryRegulation (meaning it wouldn't work with, e.g., ArbitraryPause).
     */
    private boolean isNoFishingAtStep(Regulation regulation, int step) {
        if (regulation instanceof NoFishing)
            return true;
        else if (regulation instanceof TemporaryRegulation) {
            Regulation reg = ((TemporaryRegulation) regulation).delegateAtStep(getFisher().grabState(), step);
            return isNoFishingAtStep(reg, step);
        } else if (regulation instanceof MultipleRegulations)
            return ((MultipleRegulations) regulation)
                .getRegulations().stream()
                .anyMatch(r -> isNoFishingAtStep(r, step));
        else
            return false;
    }

    /**
     * Deploying a FAD is allowed if we can fish and if there is no closure kicking in within the buffer period.
     */
    @Override
    public boolean checkIfPermitted() {
        return super.checkIfPermitted() &&
            !isNoFishingAtStep(getFisher().getRegulation(), getStep() + BUFFER_PERIOD_BEFORE_CLOSURE);
    }

    @Override
    public F getFad() {
        return fad;
    }
}
