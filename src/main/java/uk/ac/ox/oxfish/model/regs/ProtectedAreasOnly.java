package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.biology.Specie;
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
    public double maximumBiomassSellable(Fisher agent, Specie specie, FishState model) {
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
    public void reactToCatch(Catch fishCaught) {

    }

    /**
     * no reaction
     */
    @Override
    public void reactToSale(Specie specie, double biomass, double revenue) {

    }
}
