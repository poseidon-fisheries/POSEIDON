package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Basically a regulation object that it's told whether the season is open or not
 * Created by carrknight on 12/14/16.
 */
public class ExternalOpenCloseSeason implements Regulation{


    private boolean open = true;


    @Override
    public void start(FishState model, Fisher fisher) {

    }

    @Override
    public void turnOff(Fisher fisher) {

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
    public boolean canFishHere(
            Fisher agent, SeaTile tile, FishState model) {
        return open;
    }

    /**
     * how much of this species biomass is sellable. Zero means it is unsellable
     *
     * @param agent   the fisher selling its catch
     * @param species the species we are being asked about
     * @param model   a link to the model
     * @return a positive biomass if it sellable. Zero if you need to throw everything away
     */
    @Override
    public double maximumBiomassSellable(
            Fisher agent, Species species, FishState model) {
        return Double.MAX_VALUE;
    }

    /**
     * Can this fisher be at sea?
     *
     * @param fisher the  fisher
     * @param model  the model
     * @return true if it can be out. When it's false the fisher can't leave port and ought to go back to port if he is
     * at sea
     */
    @Override
    public boolean allowedAtSea(Fisher fisher, FishState model) {
        return open;
    }

    /**
     * tell the regulation object this much has been caught
     *
     * @param fishCaught catch object
     */
    @Override
    public void reactToCatch(Catch fishCaught) {

    }

    /**
     * tell the regulation object this much of this species has been sold
     *
     * @param species the species of fish sold
     * @param seller  agent selling the fish
     * @param biomass how much biomass has been sold
     * @param revenue how much money was made off it
     */
    @Override
    public void reactToSale(
            Species species, Fisher seller, double biomass, double revenue) {

    }

    /**
     * returns a copy of the regulation, used defensively
     *
     * @return
     */
    @Override
    public Regulation makeCopy() {
        ExternalOpenCloseSeason externalOpenCloseSeason = new ExternalOpenCloseSeason();
        externalOpenCloseSeason.setOpen(open);
        return externalOpenCloseSeason;
    }

    /**
     * Getter for property 'open'.
     *
     * @return Value for property 'open'.
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Setter for property 'open'.
     *
     * @param open Value to set for property 'open'.
     */
    public void setOpen(boolean open) {
        this.open = open;
    }
}
