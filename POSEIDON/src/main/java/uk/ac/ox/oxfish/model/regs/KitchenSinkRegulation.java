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

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A regulation object that is at the same time a MPA regulation + Season Limits + Quota Regulation
 * Created by carrknight on 12/9/15.
 */
public class KitchenSinkRegulation implements Regulation, QuotaPerSpecieRegulation {


    private final TemporaryProtectedArea mpaRules;

    private final FishingSeason fishingSeason;

    private final QuotaPerSpecieRegulation quotas;

    public KitchenSinkRegulation(
        TemporaryProtectedArea mpaRules, FishingSeason fishingSeason,
        QuotaPerSpecieRegulation quotas
    ) {
        this.mpaRules = mpaRules;
        fishingSeason.setRespectMPAs(false);
        this.fishingSeason = fishingSeason;
        this.quotas = quotas;
    }


    /**
     * given by the quota
     */
    @Override
    public double maximumBiomassSellable(Fisher agent, Species species, FishState model, int timeStep) {
        return Math.min(
            quotas.maximumBiomassSellable(agent, species, model, timeStep),
            this.fishingSeason.maximumBiomassSellable(agent, species, model, timeStep)
        );
    }

    /**
     * All three subrules must return true!
     */
    @Override
    public boolean canFishHere(
        Fisher agent, SeaTile tile, FishState model, int timeStep
    ) {
        return
            mpaRules.canFishHere(agent, tile, model) &&
                fishingSeason.allowedAtSea(agent, model, timeStep) &&
                quotas.allowedAtSea(agent, model, timeStep);

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
        return
            mpaRules.allowedAtSea(fisher, model, timeStep) &&
                fishingSeason.allowedAtSea(fisher, model, timeStep) &&
                quotas.allowedAtSea(fisher, model, timeStep);
    }

    /**
     * tell the regulation object this much inPenaltyBox been caught
     *
     * @param where
     * @param who
     * @param fishCaught        catch object
     * @param fishRetained
     * @param hoursSpentFishing
     */
    @Override
    public void reactToFishing(
        SeaTile where, Fisher who, Catch fishCaught, Catch fishRetained,
        int hoursSpentFishing, FishState model, int timeStep
    ) {
        mpaRules.reactToFishing(where, who, fishCaught, fishRetained, hoursSpentFishing, model, timeStep);
        fishingSeason.reactToFishing(where, who, fishCaught, fishRetained, hoursSpentFishing, model, timeStep);
        quotas.reactToFishing(where, who, fishCaught, fishRetained, hoursSpentFishing, model, timeStep);
    }

    /**
     * tell the regulation object this much of this species inPenaltyBox been sold
     *
     * @param species the species of fish sold
     * @param seller  agent selling the fish
     * @param biomass how much biomass inPenaltyBox been sold
     * @param revenue how much money was made off it
     */
    @Override
    public void reactToSale(
        Species species, Fisher seller, double biomass, double revenue, FishState model, int timeStep
    ) {
        mpaRules.reactToSale(species, seller, biomass, revenue, model, timeStep);
        fishingSeason.reactToSale(species, seller, biomass, revenue, model, timeStep);
        quotas.reactToSale(species, seller, biomass, revenue, model, timeStep);
    }

    /**
     * returns a copy of the regulation, used defensively
     *
     * @return
     */
    @Override
    public Regulation makeCopy() {
        return new KitchenSinkRegulation(
            ((TemporaryProtectedArea) mpaRules.makeCopy()),
            ((FishingSeason) fishingSeason.makeCopy()),
            ((QuotaPerSpecieRegulation) quotas.makeCopy())
        );
    }

    @Override
    public double getQuotaRemaining(int specieIndex) {
        return quotas.getQuotaRemaining(specieIndex);
    }

    @Override
    public void setQuotaRemaining(int specieIndex, double newQuotaValue) {
        quotas.setQuotaRemaining(specieIndex, newQuotaValue);
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        mpaRules.start(model, fisher);
        fishingSeason.start(model, fisher);
        quotas.start(model, fisher);
    }

    @Override
    public void turnOff(Fisher fisher) {
        mpaRules.turnOff(fisher);
        fishingSeason.turnOff(fisher);
        quotas.turnOff(fisher);
    }
}
