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

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.fads.PurseSeineGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import javax.measure.Quantity;
import javax.measure.quantity.Time;
import java.util.Optional;

import static uk.ac.ox.oxfish.utility.Measures.toHours;

/**
 * Represents either a FAD set or an unassociated set. Dolphin sets will presumable fall under this interface too.
 */
public abstract class SetAction extends PurseSeinerAction {

    private Quantity<Time> duration;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") private Optional<Catch> catchesKept = Optional.empty();

    SetAction(FishState model, Fisher fisher) {
        super(model, fisher);
        this.duration = ((PurseSeineGear) fisher.getGear()).nextSetDuration(model.getRandom());
    }

    public Optional<Catch> getCatchesKept() { return catchesKept; }

    @Override public ActionResult act(
        FishState model, Fisher fisher, Regulation regulation, double hoursLeft
    ) {
        final PurseSeineGear purseSeineGear = (PurseSeineGear) fisher.getGear();
        if (canHappen()) {
            final int duration = toHours(this.duration);
            final SeaTile seaTile = fisher.getLocation();
            if (isSuccessful(purseSeineGear, model.getRandom())) {
                final LocalBiology targetBiology = targetBiology(
                    purseSeineGear, model.getBiology(), seaTile, model.getRandom()
                );
                catchesKept = Optional.of(
                    fisher.fishHere(model.getBiology(), duration, model, targetBiology).getFirst()
                );
                model.recordFishing(seaTile); // TODO: make listener
            } else {
                reactToFailedSet(model, seaTile);
            }
            notifyFadManager();
            return new ActionResult(actionAfterSet(), hoursLeft - duration);
        } else {
            return new ActionResult(new Arriving(), hoursLeft);
        }
    }

    abstract void notifyFadManager();

    abstract boolean isSuccessful(PurseSeineGear purseSeineGear, MersenneTwisterFast rng);

    abstract LocalBiology targetBiology(
        PurseSeineGear purseSeineGear,
        GlobalBiology globalBiology,
        LocalBiology seaTileBiology,
        MersenneTwisterFast rng
    );

    void reactToFailedSet(FishState model, SeaTile locationOfSet) {}

    abstract Action actionAfterSet();

    public boolean isPossible() {
        return getFisher().getHold().getPercentageFilled() < 1 && getLocation().isWater();
    }

    public Quantity<Time> getDuration() { return duration; }

}
