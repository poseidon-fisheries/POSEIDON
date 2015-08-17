package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A regulation that allows fishing/selling/navigation only for the first x days of the year
 * Created by carrknight on 5/2/15.
 */
public class FishingSeason implements Regulation
{

    /**
     * if true MPAs are closed at all times
     */
    private boolean respectMPAs = true;

    /**
     * how many days a year is this open?
     */
    private int daysOpened;

    public FishingSeason(boolean respectMPAs, int daysOpened) {
        this.respectMPAs = respectMPAs;
        this.daysOpened = daysOpened;
    }


    private boolean seasonOpen(FishState state)
    {
        return state.getDayOfTheYear() <= daysOpened;
    }

    /**
     * can the agent fish at this location?
     *
     * @param agent the agent that wants to fish
     * @param tile  the tile the fisher is trying to fish on
     * @param model a link to the model
     * @return true if the fisher can fish
     */
    @Override
    public boolean canFishHere(Fisher agent, SeaTile tile, FishState model) {
        return  seasonOpen(model) && (!respectMPAs || !tile.isProtected());
    }

    /**
     * can sell anything as long as the fishing season is open
     */
    @Override
    public double maximumBiomassSellable(Fisher agent, Specie specie, FishState model) {
        if(seasonOpen(model))
            return Double.MAX_VALUE;
        else
            return 0;
    }

    /**
     * they can as long as the fishing season is open
     */
    @Override
    public boolean allowedAtSea(Fisher fisher, FishState model) {
        return seasonOpen(model);
    }

    /**
     * nothing
     */
    @Override
    public void reactToCatch(Catch fishCaught) {

    }

    /**
     * nothing
     */
    @Override
    public void reactToSale(Specie specie, double biomass, double revenue) {

    }

    public boolean isRespectMPAs() {
        return respectMPAs;
    }

    public int getDaysOpened() {
        return daysOpened;
    }


    /**
     * returns a copy of the regulation, used defensively
     *
     * @return
     */
    @Override
    public Regulation makeCopy() {
        return new FishingSeason(respectMPAs,daysOpened);
    }
}
