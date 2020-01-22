package uk.ac.ox.oxfish.fisher.actions.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.gear.fads.PurseSeineGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

public class MakeFadSet extends SetAction {

    public static String ACTION_NAME = "FAD sets";
    private Fad targetFad;

    public MakeFadSet(FishState model, Fisher fisher, Fad targetFad) {
        super(model, fisher);
        this.targetFad = targetFad;
    }

    public String getActionName() { return ACTION_NAME; }

    @Override boolean isSuccessful(PurseSeineGear purseSeineGear, MersenneTwisterFast rng) {
        return rng.nextDouble() < purseSeineGear.getSuccessfulSetProbability();
    }

    @Override
    public boolean isPossible() {
        return super.isPossible() && isFadHere(targetFad);
    }

    @Override public Action actionAfterSet() { return new PickUpFad(getModel(), getFisher(), targetFad); }

    /**
     * When making a FAD set, the target biology is the biology of the target FAD.
     * Fish has already been removed from the underlying sea tiles while the FAD
     * was drifting so we don't need to do that now.
     */
    @Override public LocalBiology targetBiology(
        PurseSeineGear purseSeineGear, GlobalBiology globalBiology, LocalBiology seaTileBiology, MersenneTwisterFast rng
    ) {
        return targetFad.getBiology();
    }

    /**
     * When a FAD set fails, the fish is returned to the underlying sea tile biology.
     */
    @Override public void reactToFailedSet(FishState model, SeaTile locationOfSet) {
        targetFad.releaseFish(model.getBiology().getSpecies(), locationOfSet.getBiology());
    }

}
