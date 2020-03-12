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

package uk.ac.ox.oxfish.fisher.actions.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
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

import static uk.ac.ox.oxfish.utility.Measures.toHours;

/**
 * Represents either a FAD set or an unassociated set. Dolphin sets will presumable fall under this interface too.
 * It's not lost on me that making unassociated/dolphin sets extend *Fad*Action is weird, so TODO: revise this.
 */
public abstract class SetAction extends FadAction {

    private Quantity<Time> duration;

    SetAction(FishState model, Fisher fisher) {
        super(model, fisher);
        this.duration = ((PurseSeineGear) fisher.getGear()).nextSetDuration(model.getRandom());
    }

    @Override public ActionResult act(
        FishState model, Fisher fisher, Regulation regulation, double hoursLeft
    ) {
        final PurseSeineGear purseSeineGear = (PurseSeineGear) fisher.getGear();
        if (canHappen()) {
            final int duration = toHours(this.duration);
            final SeaTile seaTile = fisher.getLocation();
            fisher.getYearlyCounter().count(totalCounterName(), 1);
            fisher.getYearlyCounter().count(regionCounterName(), 1);
            if (isSuccessful(purseSeineGear, model.getRandom())) {
                final LocalBiology targetBiology = targetBiology(
                    purseSeineGear, model.getBiology(), seaTile, model.getRandom()
                );
                final Catch catchesKept = fisher.fishHere(model.getBiology(), duration, model, targetBiology).getFirst();
                model.getBiology().getSpecies().forEach(species ->
                    fisher.getYearlyCounter().count(catchesCounterName(species), catchesKept.getWeightCaught(species))
                );
                getFadManager().getActionSpecificRegulations().reactToAction(this);
                model.recordFishing(seaTile);
            } else {
                reactToFailedSet(model, seaTile);
            }
            return new ActionResult(actionAfterSet(), hoursLeft - duration);
        } else {
            return new ActionResult(new Arriving(), hoursLeft);
        }
    }

    abstract boolean isSuccessful(PurseSeineGear purseSeineGear, MersenneTwisterFast rng);

    abstract LocalBiology targetBiology(PurseSeineGear purseSeineGear, GlobalBiology globalBiology, LocalBiology seaTileBiology, MersenneTwisterFast rng);

    public String catchesCounterName(Species species) {
        return catchesCounterName(species.getName(), getActionName());
    }

    void reactToFailedSet(FishState model, SeaTile locationOfSet) {}

    abstract Action actionAfterSet();

    public static String catchesCounterName(String speciesName, String actionName) {
        return speciesName + " catches from " + actionName;
    }

    public boolean isPossible() {
        return getFisher().getHold().getPercentageFilled() < 1 && getSeaTile().isWater();
    }

    public Quantity<Time> getDuration() { return duration; }

}
