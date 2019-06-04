package uk.ac.ox.oxfish.fisher.actions.fads;

import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.getFadManager;
import static uk.ac.ox.oxfish.utility.Measures.toHours;

import java.util.Optional;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.gear.PurseSeineGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import javax.measure.quantity.Time;

public class MakeFadSet implements FadAction {

    private Fad targetFad;

    public MakeFadSet(Fad targetFad) { this.targetFad = targetFad; }

    private boolean isFadHere(Fisher fisher) {
        return getActionTile(fisher)
            .filter(fadTile -> fadTile.equals(fisher.getLocation()))
            .isPresent();
    }

    private boolean isFishingAllowedHere(FishState model, Fisher fisher) {
        return fisher.getRegulation().canFishHere(fisher, fisher.getLocation(), model);
    }

    public boolean isPossible(FishState model, Fisher fisher) {
        return isFadHere(fisher) && (isFishingAllowedHere(model, fisher) || fisher.isCheater());
    }

    private PurseSeineGear getPurseSeineGear(Fisher fisher) {
        if (fisher.getGear() instanceof PurseSeineGear)
            return (PurseSeineGear) fisher.getGear();
        else throw new IllegalStateException(
            "MakeFadSet action can only be used with PurseSeineGear.");
    }

    @Override
    public ActionResult act(
        FishState model, Fisher fisher, Regulation regulation, double hoursLeft
    ) {
        // TODO: Gala told us that FAD sets are usually made at dawn. How should this be handled?
        if (isPossible(model, fisher)) {
            // TODO: should FAD sets follow the same "accrued hours" logic as `Fishing`?
            final int duration = toHours(getDuration());
            fisher.fishHere(model.getBiology(), duration, model, targetFad.getAggregatedBiology());
            model.recordFishing(fisher.getLocation());
            // TODO: picking up the FAD might not always be the thing to do
            return new ActionResult(new PickUpFad(targetFad), hoursLeft - duration);
        } else {
            // it can happen that the FAD has drifted away or the fishing season ended since the
            // action was decided, in which case the fisher has to reconsider its course of action
            // TODO: if the FAD has drifted away, should the fisher keep pursuing it?
            return new ActionResult(new Arriving(), hoursLeft);
        }
    }

    @Override
    public Optional<SeaTile> getActionTile(Fisher fisher) {
        return getFadManager(fisher).getFadMap().getFadTile(targetFad);
    }

    @Override public Time getDuration() {
        return PurseSeineGear.DURATION_OF_SET;
    }

}
