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

    SetAction(PurseSeineGear purseSeineGear, MersenneTwisterFast rng) {
        this.duration = purseSeineGear.nextSetDuration(rng);
    }

    @Override public ActionResult act(
        FishState model, Fisher fisher, Regulation regulation, double hoursLeft
    ) {
        final PurseSeineGear purseSeineGear = (PurseSeineGear) fisher.getGear();
        if (isAllowed(model, fisher) && isPossible(model, fisher)) {
            final int duration = toHours(this.duration);
            final SeaTile seaTile = fisher.getLocation();
            fisher.getYearlyCounter().count(totalCounterName(), 1);
            fisher.getYearlyCounter().count(regionCounterName(model.getMap(), seaTile), 1);
            if (model.getRandom().nextDouble() < purseSeineGear.getSuccessfulSetProbability()) {
                final LocalBiology targetBiology = targetBiology(
                    purseSeineGear, model.getBiology(), seaTile, model.getRandom()
                );
                final Catch catchesKept = fisher.fishHere(model.getBiology(), duration, model, targetBiology).getFirst();
                model.getBiology().getSpecies().forEach(species ->
                    fisher.getYearlyCounter().count(catchesCounterName(species), catchesKept.getWeightCaught(species))
                );
                model.recordFishing(seaTile);
            } else {
                reactToFailedSet(model, seaTile);
            }
            return new ActionResult(actionAfterSet(), hoursLeft - duration);
        } else {
            return new ActionResult(new Arriving(), hoursLeft);
        }
    }

    public boolean isPossible(FishState model, Fisher fisher) {
        return fisher.getHold().getPercentageFilled() < 1 && fisher.getLocation().isWater();
    }

    abstract LocalBiology targetBiology(PurseSeineGear purseSeineGear, GlobalBiology globalBiology, LocalBiology seaTileBiology, MersenneTwisterFast rng);

    public String catchesCounterName(Species species) {
        return catchesCounterName(species.getName(), getActionName());
    }

    void reactToFailedSet(FishState model, SeaTile locationOfSet) {}

    abstract Action actionAfterSet();

    public static String catchesCounterName(String speciesName, String actionName) {
        return speciesName + " catches from " + actionName;
    }

    public Quantity<Time> getDuration() { return duration; }

}
