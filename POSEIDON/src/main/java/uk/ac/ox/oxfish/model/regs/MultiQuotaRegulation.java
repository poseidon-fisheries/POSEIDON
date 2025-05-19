/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.regs;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Yearly resetting biomass quotas (different for each specie), counted at landing. If any quota is at 0, no
 * other specie is tradeable
 * Created by carrknight on 10/7/15.
 */
public class MultiQuotaRegulation implements QuotaPerSpecieRegulation, Steppable {

    private static final long serialVersionUID = -5481639270374933438L;
    protected final double[] quotaRemaining;
    private final double[] yearlyQuota;
    private final FishState state;

    private boolean respectMPA;

    public MultiQuotaRegulation(final double[] yearlyQuota, final FishState state) {
        this.yearlyQuota = Arrays.copyOf(yearlyQuota, yearlyQuota.length);
        this.quotaRemaining = Arrays.copyOf(yearlyQuota, yearlyQuota.length);
        for (final double aQuotaRemaining : quotaRemaining) {
            assert aQuotaRemaining >= 0;
        }
        this.state = state;
        this.state.scheduleEveryYear(this, StepOrder.POLICY_UPDATE);
        respectMPA = true;
    }


    public MultiQuotaRegulation(
        final double[] yearlyQuota, final FishState state,
        final int quotaPeriod
    ) {
        this.yearlyQuota = Arrays.copyOf(yearlyQuota, yearlyQuota.length);
        this.quotaRemaining = Arrays.copyOf(yearlyQuota, yearlyQuota.length);
        for (final double aQuotaRemaining : quotaRemaining) {
            assert aQuotaRemaining >= 0;
        }
        this.state = state;
        this.state.scheduleEveryXDay(this, StepOrder.POLICY_UPDATE, quotaPeriod);
        respectMPA = true;
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
        final Fisher agent, final Species species, final FishState model, final int timeStep
    ) {
        return quotaRemaining[species.getIndex()] + FishStateUtilities.EPSILON / 2;
    }

    /**
     * returns a copy of the regulation, used defensively
     *
     * @return
     */
    @Override
    public Regulation makeCopy() {
        return new MultiQuotaRegulation(yearlyQuota, state);
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
    public boolean allowedAtSea(final Fisher fisher, final FishState model, final int timeStep) {
        return isFishingStillAllowed();

    }

    public boolean isFishingStillAllowed() {

        //all quotas must be at least 0 and at least one must be ABOVE 0
        boolean above0 = false;
        for (final double quota : quotaRemaining) {
            if (quota < 0)
                return false;
            if (quota > FishStateUtilities.EPSILON)
                above0 = true;
        }

        return above0;

    }

    /**
     * burn through quotas; because of "maximum biomass sellable"  method, I expect here that the biomass
     * sold is less or equal to the quota available
     *
     * @param species the species of fish sold
     * @param seller  agent selling the fish
     * @param biomass how much biomass has been sold
     * @param revenue how much money was made off it
     */
    @Override
    public void reactToSale(
        final Species species,
        final Fisher seller,
        final double biomass,
        final double revenue,
        final FishState model,
        final int timeStep
    ) {
        final double newQuota = quotaRemaining[species.getIndex()] - biomass;
        Logger.getGlobal().fine(() ->
            "lowering quota for " + species + " owned by " + seller + "to " + newQuota
        );
        setQuotaRemaining(species.getIndex(), newQuota);
        Preconditions.checkArgument(
            quotaRemaining[species.getIndex()] >= -FishStateUtilities.EPSILON,
            quotaRemaining[species.getIndex()]
        );
    }

    @Override
    public void setQuotaRemaining(final int specieIndex, final double newQuotaValue) {

        quotaRemaining[specieIndex] = newQuotaValue;
        Preconditions.checkArgument(newQuotaValue >= -FishStateUtilities.EPSILON);

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
        final Fisher agent, final SeaTile tile, final FishState model, final int timeStep
    ) {
        return isFishingStillAllowed() && (!tile.isProtected() || !respectMPA);
    }

    public void setYearlyQuota(final int specieIndex, final double newQuotaValue) {
        yearlyQuota[specieIndex] = newQuotaValue;
        if (quotaRemaining[specieIndex] >= yearlyQuota[specieIndex])
            setQuotaRemaining(specieIndex, newQuotaValue);
        Preconditions.checkArgument(newQuotaValue >= -FishStateUtilities.EPSILON);

    }

    @Override
    public double getQuotaRemaining(final int specieIndex) {
        return quotaRemaining[specieIndex];
    }

    @Override
    public void step(final SimState simState) {
        System.arraycopy(yearlyQuota, 0, quotaRemaining, 0, quotaRemaining.length);

    }

    /**
     * Getter for property 'quotaRemaining'.
     *
     * @return Value for property 'quotaRemaining'.
     */
    public double[] getQuotaRemaining() {
        return quotaRemaining;
    }

    protected FishState getState() {
        return state;
    }

    public int getNumberOfSpeciesTracked() {
        return getYearlyQuota().length;
    }

    public double[] getYearlyQuota() {
        return yearlyQuota;
    }

    /**
     * Getter for property 'respectMPA'.
     *
     * @return Value for property 'respectMPA'.
     */
    public boolean isRespectMPA() {
        return respectMPA;
    }

    /**
     * Setter for property 'respectMPA'.
     *
     * @param respectMPA Value to set for property 'respectMPA'.
     */
    public void setRespectMPA(final boolean respectMPA) {
        this.respectMPA = respectMPA;
    }
}
