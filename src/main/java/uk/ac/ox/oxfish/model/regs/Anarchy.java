package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * This is basically no-rules. It's always valid to fish (even in an MPA) and no limits are ever imposed
 */
public class Anarchy implements Regulation {
    /**
     * can always fish
     */
    @Override
    public boolean canFishHere(Fisher agent, SeaTile tile, FishState model) {
        return true;
    }

    /**
     *  return maximum double
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
