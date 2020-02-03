package uk.ac.ox.oxfish.fisher.actions.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.equipment.gear.fads.PurseSeineGear;
import uk.ac.ox.oxfish.model.FishState;

public class MakeUnassociatedSet extends SetAction {

    public static String ACTION_NAME = "unassociated sets";

    public MakeUnassociatedSet(FishState model, Fisher fisher) {
        super(model, fisher);
    }

    @Override boolean isSuccessful(PurseSeineGear purseSeineGear, MersenneTwisterFast rng) {
        // unassociated sets are always successful since we're sampling from an empirical distribution
        // that includes failed sets with zeros for all species.
        return true;
    }

    public String getActionName() { return ACTION_NAME; }

    @Override public Action actionAfterSet() { return new Arriving(); }

    /**
     * The target biology of an unassociated set has to be created on the fly, and this action is delegated to
     * the purse seine gear, which knows how much fish its likely to catch. Note that, since this is only done
     * in the case of a successful set, there is no need for a separate method to release the fish if it fails.
     */
    @Override public LocalBiology targetBiology(
        PurseSeineGear purseSeineGear,
        GlobalBiology globalBiology,
        LocalBiology seaTileBiology,
        MersenneTwisterFast rng
    ) {
        return purseSeineGear.createUnassociatedSetBiology(globalBiology, seaTileBiology, rng);
    }

}
