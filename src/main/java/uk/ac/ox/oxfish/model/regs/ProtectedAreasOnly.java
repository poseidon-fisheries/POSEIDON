package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * No limits on catch but you can only fish outside of MPAs
 * Created by carrknight on 5/2/15.
 */
public class ProtectedAreasOnly implements Regulation {
    /**
     * can the agent fish at this location?
     *
     * @param agent the agent that wants to fish
     * @param tile  the tile the fisher is trying to fish on
     * @param model
     * @return true if the fisher can fish
     */
    @Override
    public boolean canFishHere(Fisher agent, SeaTile tile, FishState model) {
        return !tile.isProtected();
    }

    /**
     *  can always sell
     */
    @Override
    public double maximumBiomassSellable(Fisher agent, Species species, FishState model) {
        return Double.MAX_VALUE;

    }

    /**
     * Can always leave
     */
    @Override
    public boolean allowedAtSea(Fisher fisher, FishState model) {
        return true;
    }

    /**
     * no reaction
     */
    @Override
    public void reactToFishing(SeaTile where, Fisher who, Catch fishCaught, int hoursSpentFishing) {

    }

    /**
     * no reaction
     */
    @Override
    public void reactToSale(Species species, Fisher seller, double biomass, double revenue) {

    }

    /**
     * returns a copy of the regulation, used defensively
     *
     * @return
     */
    @Override
    public Regulation makeCopy() {
        return new ProtectedAreasOnly();
    }

    /**
     * ignored
     */
    @Override
    public void start(FishState model, Fisher fisher) {

    }

    /**
     * ignored
     */
    @Override
    public void turnOff(Fisher fisher) {

    }
}
