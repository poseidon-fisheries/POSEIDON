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
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import javax.measure.Quantity;
import javax.measure.quantity.Time;

import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.HOUR;
import static uk.ac.ox.oxfish.utility.Measures.toHours;

public class PickUpFad extends PurseSeinerAction {

    public static String ACTION_NAME = "FAD pickups";
    private final Fad targetFad;

    PickUpFad(FishState model, Fisher fisher, Fad targetFad) {
        super(model, fisher);
        this.targetFad = targetFad;
    }

    @Override
    public ActionResult act(
        FishState model, Fisher fisher, Regulation regulation, double hoursLeft
    ) {
        if (isPossible()) {
            getFadManager().pickUpFad(targetFad);
            return new ActionResult(new Arriving(), hoursLeft - toHours(getDuration()));
        } else {
            // it can happen that the FAD has drifted away, in which case the fisher has to
            // reconsider its course of action
            // TODO: if the FAD has drifted away, should the fisher keep pursuing it?
            return new ActionResult(new Arriving(), hoursLeft);
        }
    }

    @Override public boolean isPossible() { return isFadHere(targetFad); }

    // Since FADs are (currently) always picked up after a set, we consider that the time
    // to pick them up is included in the time it takes to make a set.
    @Override public Quantity<Time> getDuration() { return getQuantity(0, HOUR); }

    @Override String getActionName() { return ACTION_NAME; }

}
