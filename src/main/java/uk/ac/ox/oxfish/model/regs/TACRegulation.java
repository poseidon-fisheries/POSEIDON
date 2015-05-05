package uk.ac.ox.oxfish.model.regs;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

/**
 * Fixed quota after which the season stops till the end of the year.
 * The quota gets counted only when the fish is sold at the market
 * Created by carrknight on 5/2/15.
 */
public class TACRegulation implements Regulations, Steppable {


    public static final double EPSILON = .1;
    /**
     * how much biomass in total can be caught each year
     */
    private double yearlyQuota;
    /**
     * how much biomass is still available to be caught this year
     */
    private double quotaRemaining;


    /**
     * when created it sets itself to step every year to reset the quota
     * @param yearlyQuota the yearly quota
     * @param state the model link to schedule on
     */
    public TACRegulation(double yearlyQuota, FishState state) {
        this.yearlyQuota = yearlyQuota;
        this.quotaRemaining = yearlyQuota;
        state.scheduleEveryYear(this, StepOrder.AFTER_FISHER_PHASE);
    }

    private boolean isFishingStillAllowed(){
        return quotaRemaining > EPSILON;
    }


    /**
     * this regulation step resets the quota remaining
     * @param simState the quota
     */
    @Override
    public void step(SimState simState) {
        quotaRemaining = yearlyQuota;
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
        return isFishingStillAllowed();
    }

    /**
     * how much of this specie biomass is sellable. Zero means it is unsellable
     *
     * @param agent  the fisher selling its catch
     * @param specie the specie we are being asked about
     * @param model  a link to the model
     * @return a positive biomass if it sellable. Zero if you need to throw everything away
     */
    @Override
    public double maximumBiomassSellable(
            Fisher agent, Specie specie, FishState model) {
        return quotaRemaining;
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
        return isFishingStillAllowed();
    }

    /**
     *  reacts only to fish sold
     */
    @Override
    public void reactToCatch(Catch fishCaught) {
    }

    /**
     * tell the regulation object this much of this specie has been sold
     *
     * @param specie  the specie of fish sold
     * @param biomass how much biomass has been sold
     * @param revenue how much money was made off it
     */
    @Override
    public void reactToSale(Specie specie, double biomass, double revenue) {

        quotaRemaining -= biomass;
        Preconditions.checkState(quotaRemaining >= 0, quotaRemaining);
    }
}
