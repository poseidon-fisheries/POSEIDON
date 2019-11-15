package uk.ac.ox.oxfish.fisher.actions.fads;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.gear.PurseSeineGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.Optional;

import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.getFadManager;
import static uk.ac.ox.oxfish.utility.Measures.toHours;

public class MakeFadSet implements SetAction {

    private Fad targetFad;

    public MakeFadSet(Fad targetFad) {
        this.targetFad = targetFad;
    }

    @Override
    public ActionResult act(
        FishState model, Fisher fisher, Regulation regulation, double hoursLeft
    ) {
        final PurseSeineGear purseSeineGear = (PurseSeineGear) fisher.getGear();
        if (isAllowed(model, fisher) && isPossible(model, fisher)) {
            // TODO: should FAD sets follow the same "accrued hours" logic as `Fishing`?
            final int duration = toHours(getDuration(fisher, model.getRandom()));
            if (model.getRandom().nextDouble() < purseSeineGear.getSuccessfulSetProbability()) {
                fisher.fishHere(model.getBiology(), duration, model, targetFad.getBiology());
                model.recordFishing(fisher.getLocation());
            } else {
                targetFad.releaseFish(model.getBiology().getSpecies(), fisher.getLocation().getBiology());
            }
            return new ActionResult(new PickUpFad(targetFad), hoursLeft - duration);
        } else {
            // it can happen that the FAD has drifted away or the fishing season ended since the
            // action was decided, in which case the fisher has to reconsider its course of action
            return new ActionResult(new Arriving(), hoursLeft);
        }
    }

    @Override
    public boolean isPossible(FishState model, Fisher fisher) {
        return SetAction.super.isPossible(model,fisher) && isFadHere(fisher);
    }

    private boolean isFadHere(Fisher fisher) {
        return getActionTile(fisher)
            .filter(fadTile -> fadTile.equals(fisher.getLocation()))
            .isPresent();
    }

    @Override
    public Optional<SeaTile> getActionTile(Fisher fisher) {
        return getFadManager(fisher).getFadMap().getFadTile(targetFad);
    }

}
