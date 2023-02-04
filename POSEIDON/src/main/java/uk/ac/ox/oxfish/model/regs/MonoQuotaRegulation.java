/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.model.regs;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Fixed biomass quota (no species difference) after which the season stops till the end of the year.
 * The quota gets counted only when the fish is sold at the market
 * Created by carrknight on 5/2/15.
 */
public class MonoQuotaRegulation implements QuotaPerSpecieRegulation, Steppable {


    /**
     * how much biomass in total can be caught each year
     */
    private double yearlyQuota;
    /**
     * how much biomass is still available to be caught this year
     */
    private double quotaRemaining;

    /**
     * if this is set to anything above 0, then quota season is in this many days rather than yearly
     */
    private final int quotaPeriodInDays;


    /**
     * when created it sets itself to step every year to reset the quota
     * @param yearlyQuota the yearly quota
     *
     */

    public MonoQuotaRegulation(double yearlyQuota) {

        this(yearlyQuota,-1);
    }

    public MonoQuotaRegulation(double yearlyQuota, int quotaPeriodInDays) {
        this.yearlyQuota = yearlyQuota;
        this.quotaRemaining = yearlyQuota;
        this.quotaPeriodInDays = quotaPeriodInDays;
    }

    private boolean isFishingStillAllowed(){
        return quotaRemaining > FishStateUtilities.EPSILON;
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
            Fisher agent, SeaTile tile, FishState model, int timeStep) {
        return isFishingStillAllowed();
    }

    /**
     * how much of this species biomass is sellable. Zero means it is unsellable
     *
     * @param agent  the fisher selling its catch
     * @param species the species we are being asked about
     * @param model  a link to the model
     * @return a positive biomass if it sellable. Zero if you need to throw everything away
     */
    @Override
    public double maximumBiomassSellable(
            Fisher agent, Species species, FishState model, int timeStep) {
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
    public boolean allowedAtSea(Fisher fisher, FishState model, int timeStep) {
        return isFishingStillAllowed();
    }

    /**
     * tell the regulation object this much of this species has been sold
     *  @param species  the species of fish sold
     * @param seller
     * @param biomass how much biomass has been sold
     * @param revenue how much money was made off it
     */
    @Override
    public void reactToSale(Species species, Fisher seller, double biomass, double revenue, FishState model, int timeStep) {

        quotaRemaining -= biomass;
        Preconditions.checkState(quotaRemaining >= -FishStateUtilities.EPSILON, quotaRemaining);
    }

    public double getYearlyQuota() {
        return yearlyQuota;
    }

    /**
     * get quota remaining
     * @param specieIndex ignored
     * @return
     */
    public double getQuotaRemaining(int specieIndex) {
        return quotaRemaining;
    }

    public void setYearlyQuota(double yearlyQuota) {
        this.yearlyQuota = yearlyQuota;
    }

    /**
     * set total quota remaining. specie index is completely ignored
     * @param specieIndex ignored since the quota is for any specie
     * @param quotaRemaining the quota remaining
     */
    public void setQuotaRemaining(int specieIndex, double quotaRemaining) {
        this.quotaRemaining = quotaRemaining;
        Preconditions.checkArgument(quotaRemaining >= 0);
    }

    /**
     * returns a copy of the regulation, used defensively
     *
     * @return
     */
    @Override
    public Regulation makeCopy() {
        return new MonoQuotaRegulation(yearlyQuota);
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        if(quotaPeriodInDays<=0)
            model.scheduleEveryYear(this, StepOrder.POLICY_UPDATE);
        else
            model.scheduleEveryXDay(this,StepOrder.POLICY_UPDATE,quotaPeriodInDays);

    }

}
