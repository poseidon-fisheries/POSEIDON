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
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;

/**
 * Yearly resetting biomass quotas (different for each specie), counted at landing. If any quota is at 0, no
 * other specie is tradeable
 * Created by carrknight on 10/7/15.
 */
public class MultiQuotaRegulation implements  QuotaPerSpecieRegulation,Steppable
{

    private final double[] yearlyQuota;

    private final double[] quotaRemaining;
    private final FishState state;

    public MultiQuotaRegulation(double[] yearlyQuota, FishState state) {
        this.yearlyQuota = Arrays.copyOf(yearlyQuota,yearlyQuota.length);
        this.quotaRemaining = Arrays.copyOf(yearlyQuota,yearlyQuota.length);
        for (double aQuotaRemaining : quotaRemaining) {
            assert aQuotaRemaining >= 0;
        }
        this.state = state;
        this.state.scheduleEveryYear(this, StepOrder.POLICY_UPDATE);
    }


    public boolean isFishingStillAllowed(){

        //all must be strictly positive!
        return
                Arrays.stream(quotaRemaining).allMatch(value -> value > FishStateUtilities.EPSILON);


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
        return quotaRemaining[specie.getIndex()];
    }


    /**
     * returns a copy of the regulation, used defensively
     *
     * @return
     */
    @Override
    public Regulation makeCopy() {
        return new MultiQuotaRegulation(yearlyQuota,state);
    }

    /**
     * ignored
     *
     * @param fishCaught catch object
     */
    @Override
    public void reactToCatch(Catch fishCaught) {
        //ignored
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




    /**burn through quotas; because of "maximum biomass sellable"  method, I expect here that the biomass
     * sold is less or equal to the quota available
     *
     * @param specie  the specie of fish sold
     * @param seller  agent selling the fish
     * @param biomass how much biomass has been sold
     * @param revenue how much money was made off it
     */
    @Override
    public void reactToSale(Specie specie, Fisher seller, double biomass, double revenue) {
        quotaRemaining[specie.getIndex()]-=biomass;
        Preconditions.checkArgument(quotaRemaining[specie.getIndex()]>=0);
    }

    /**
     * can fish as long as it is not an MPA and still has all quotas
     *
     * @param agent the agent that wants to fish
     * @param tile  the tile the fisher is trying to fish on
     * @param model a link to the model
     * @return true if the fisher can fish
     */
    @Override
    public boolean canFishHere(
            Fisher agent, SeaTile tile, FishState model) {
        return isFishingStillAllowed() && !tile.isProtected();
    }

    @Override
    public void setQuotaRemaining(int specieIndex, double newQuotaValue) {
        quotaRemaining[specieIndex] = newQuotaValue;
        Preconditions.checkArgument(newQuotaValue >= 0);

    }

    @Override
    public double getQuotaRemaining(int specieIndex) {
        return quotaRemaining[specieIndex];
    }

    @Override
    public void step(SimState simState) {
        System.arraycopy(yearlyQuota, 0, quotaRemaining, 0, quotaRemaining.length);

    }

    protected double[] getYearlyQuota() {
        return yearlyQuota;
    }

    protected double[] getQuotaRemaining() {
        return quotaRemaining;
    }

    protected FishState getState() {
        return state;
    }
}
