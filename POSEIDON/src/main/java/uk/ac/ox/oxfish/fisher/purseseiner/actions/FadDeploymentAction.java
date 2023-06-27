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

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.ConjunctiveRegulations;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.TaggedRegulation;

import java.util.stream.Stream;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class FadDeploymentAction extends PurseSeinerAction implements FadRelatedAction {

    // TODO: this should ideally be configurable, but we'll need to implement
    //       temporary regulation at the action specific level for that.
    private static final int BUFFER_PERIOD_BEFORE_CLOSURE = 15;

    private Fad fad;

    public FadDeploymentAction(final Fisher fisher) {
        super(
            fisher,
            fisher.getLocation(),
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
        final FadManager fadManager = getFadManager(fisher);
        this.fad = fadManager.deployFad(getLocation(), fishState.random);
        setTime(hoursLeft);
        fadManager.reactTo(this);
        return new ActionResult(new Arriving(), Math.max(0, hoursLeft - getDuration()));
    }

    /**
     * This little piece of ugliness is my "solution" to the problem of disallowing FAD deployments 15 days before
     * the start of a temporary closure. It recursively digs down the regulation hierarchy to see if a regulation
     * tagged "closure A" or "closure B" will be active at the specified step.
     * <p>
     * The proper way to do this would be to have a system of temporary action-specific regulations (just like
     * we have of old-school regulations) and use that to disallow deployments before closures.
     */
    private boolean isNoFishingAtStep(final Regulation regulation, final int step) {
        if (regulation instanceof ConjunctiveRegulations) {
            return ((ConjunctiveRegulations) regulation)
                .getRegulations()
                .stream()
                .anyMatch(r -> isNoFishingAtStep(r, step));
        } else if (regulation instanceof TaggedRegulation) {
            final boolean isClosure =
                Stream.of("closure A", "closure B")
                    .anyMatch(((TaggedRegulation) regulation).getTags()::contains);
            return isClosure && !regulation.canFishHere(
                getFisher(),
                getFisher().getLocation(),
                getFisher().grabState(),
                step
            );
        } else {
            return false;
        }
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
    public Fad getFad() {
        return fad;
    }

    @Override
    public String getCode() {
        return "DPL";
    }
}
