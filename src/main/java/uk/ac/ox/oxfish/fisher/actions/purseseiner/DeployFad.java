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

package uk.ac.ox.oxfish.fisher.actions.purseseiner;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultipleRegulations;
import uk.ac.ox.oxfish.model.regs.NoFishing;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.TemporaryRegulation;

import javax.measure.Quantity;
import javax.measure.quantity.Time;

import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.HOUR;
import static uk.ac.ox.oxfish.utility.Measures.toHours;

public class DeployFad extends PurseSeinerAction {

    // TODO: that should probably be configurable, but there is no good place to put it...
    private static final int BUFFER_PERIOD_BEFORE_CLOSURE = 15;
    public static String ACTION_NAME = "FAD deployments";

    public DeployFad(FishState model, Fisher fisher) { super(model, fisher); }

    /**
     * This little piece of ugliness is my "solution" to the problem of disallowing FAD deployments 15 days before
     * the start of a temporary closure. It recursively digs down the regulation hierarchy to see if a NoFishing
     * regulation will be active at the specified step. It currently assumes that the regulation is some combination
     * of MultipleRegulations and TemporaryRegulation (meaning it wouldn't work with, e.g., ArbitraryPause).
     * The proper way to handle something like this would be to build the concept of "action specific regulations"
     * into the whole regulation system, but I fear that would cross the line from refactoring to re-architecturing.
     */
    private boolean isNoFishingAtStep(Regulation regulation, int step) {
        if (regulation instanceof NoFishing)
            return true;
        else if (regulation instanceof TemporaryRegulation)
            return isNoFishingAtStep(((TemporaryRegulation) regulation).delegateAtStep(getModel(), step), step);
        else if (regulation instanceof MultipleRegulations)
            return ((MultipleRegulations) regulation)
                .getRegulations().stream()
                .anyMatch(r -> isNoFishingAtStep(r, step));
        else
            return false;
    }

    /**
     * Deploying a FAD is allowed if we can fish and if there is no closure kicking in within the buffer period.
     */
    @Override public boolean isAllowed() {
        return super.isAllowed() &&
            !isNoFishingAtStep(getFisher().getRegulation(), getStep() + BUFFER_PERIOD_BEFORE_CLOSURE);
    }

    @Override String getActionName() { return ACTION_NAME; }

    @Override
    public ActionResult act(
        FishState model, Fisher fisher, Regulation regulation, double hoursLeft
    ) {
        if (canHappen()) {
            SeaTile here = fisher.getLocation();
            getFadManager().deployFad(here, model.getStep(), model.random); // TODO: make this a listener?
            getFadManager().reactTo(this);
        }
        return new ActionResult(new Arriving(), hoursLeft - toHours(getDuration()));
    }

    @Override public Quantity<Time> getDuration() {
        // see https://github.com/poseidon-fisheries/tuna/issues/6
        return getQuantity(0, HOUR);
    }

    @Override public boolean isPossible() {
        return getLocation().isWater() && getFadManager().getNumFadsInStock() > 0;
    }

}
