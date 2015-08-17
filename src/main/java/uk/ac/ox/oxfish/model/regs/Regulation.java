package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * The regulations object tell agents whether they can fish or not, where they can and whether they can sell their catch
 * or not
 * Created by carrknight on 5/2/15.
 */
public interface Regulation
{


    /**
     * can the agent fish at this location?
     * @param agent the agent that wants to fish
     * @param tile the tile the fisher is trying to fish on
     * @param model a link to the model
     * @return true if the fisher can fish
     */
    boolean canFishHere(Fisher agent, SeaTile tile, FishState model);

    /**
     * how much of this specie biomass is sellable. Zero means it is unsellable
     * @param agent the fisher selling its catch
     * @param specie the specie we are being asked about
     * @param model a link to the model
     * @return a positive biomass if it sellable. Zero if you need to throw everything away
     */
    double maximumBiomassSellable(Fisher agent, Specie specie, FishState model);

    /**
     * Can this fisher be at sea?
     * @param fisher the  fisher
     * @param model the model
     * @return true if it can be out. When it's false the fisher can't leave port and ought to go back to port if he is
     * at sea
     */
    boolean allowedAtSea(Fisher fisher, FishState model);

    /**
     * tell the regulation object this much has been caught
     * @param fishCaught catch object
     */
    void reactToCatch(Catch fishCaught);

    /**
     * tell the regulation object this much of this specie has been sold
     * @param specie the specie of fish sold
     * @param biomass how much biomass has been sold
     * @param revenue how much money was made off it
     */
    void reactToSale(Specie specie, double biomass, double revenue);

    /**
     * returns a copy of the regulation, used defensively
     * @return
     */
    Regulation makeCopy();
}
