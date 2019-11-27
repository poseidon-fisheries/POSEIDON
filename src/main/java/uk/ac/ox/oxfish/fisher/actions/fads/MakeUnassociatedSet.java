package uk.ac.ox.oxfish.fisher.actions.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.equipment.gear.fads.PurseSeineGear;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Optional;

public class MakeUnassociatedSet extends SetAction {

    public static final String NUMBER_OF_UNASSOCIATED_SETS = "Total number of unassociated sets";

    public MakeUnassociatedSet(PurseSeineGear purseSeineGear, MersenneTwisterFast rng) {
        super(purseSeineGear, rng);
    }

    @Override public String counterName() { return NUMBER_OF_UNASSOCIATED_SETS; }

    @Override
    public Optional<SeaTile> getActionTile(Fisher fisher) {
        return Optional.of(fisher.getLocation());
    }

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
