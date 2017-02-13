package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;

/**
 * The regulations object tell agents whether they can fish or not, where they can and whether they can sell their catch
 * or not
 * Created by carrknight on 5/2/15.
 */
public interface Regulation extends FisherStartable
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
     * how much of this species biomass is sellable. Zero means it is unsellable
     * @param agent the fisher selling its catch
     * @param species the species we are being asked about
     * @param model a link to the model
     * @return a positive biomass if it sellable. Zero if you need to throw everything away
     */
    double maximumBiomassSellable(Fisher agent, Species species, FishState model);

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
     * @param where where the fishing occurred
     * @param who who did the fishing
     * @param fishCaught catch object
     * @param hoursSpentFishing how many hours were spent fishing
     */
    void reactToFishing(SeaTile where, Fisher who, Catch fishCaught, int hoursSpentFishing);

    /**
     * tell the regulation object this much of this species has been sold
     * @param species the species of fish sold
     * @param seller agent selling the fish
     * @param biomass how much biomass has been sold
     * @param revenue how much money was made off it
     */
    void reactToSale(Species species, Fisher seller, double biomass, double revenue);

    /**
     * returns a copy of the regulation, used defensively
     * @return
     */
    Regulation makeCopy();
}
