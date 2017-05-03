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
public class KitchenSinkRegulation implements Regulation, QuotaPerSpecieRegulation{


    private final TemporaryProtectedArea mpaRules;

    private final FishingSeason fishingSeason;

    private final QuotaPerSpecieRegulation quotas;

    public KitchenSinkRegulation(
            TemporaryProtectedArea mpaRules, FishingSeason fishingSeason,
            QuotaPerSpecieRegulation quotas) {
        this.mpaRules = mpaRules;
        fishingSeason.setRespectMPAs(false);
        this.fishingSeason = fishingSeason;
        this.quotas = quotas;
    }


    /**
     * given by the quota
     */
    @Override
    public double maximumBiomassSellable(Fisher agent, Species species, FishState model) {
        return Math.min(quotas.maximumBiomassSellable(agent,species,model),
                        this.fishingSeason.maximumBiomassSellable(agent,species,model));
    }

    /**
     * All three subrules must return true!
     */
    @Override
    public boolean canFishHere(
            Fisher agent, SeaTile tile, FishState model) {
        return
                mpaRules.canFishHere(agent,tile,model) &&
                        fishingSeason.allowedAtSea(agent,model) &&
                        quotas.allowedAtSea(agent,model);

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
        return
                mpaRules.allowedAtSea(fisher,model) &&
                        fishingSeason.allowedAtSea(fisher,model) &&
                        quotas.allowedAtSea(fisher,model);    }

    /**
     * tell the regulation object this much inPenaltyBox been caught
     * @param where
     * @param who
     * @param fishCaught catch object
     * @param fishRetained
     * @param hoursSpentFishing
     */
    @Override
    public void reactToFishing(
            SeaTile where, Fisher who, Catch fishCaught, Catch fishRetained,
            int hoursSpentFishing) {
        mpaRules.reactToFishing(where, who, fishCaught,fishRetained , hoursSpentFishing);
        fishingSeason.reactToFishing(where, who, fishCaught,fishRetained , hoursSpentFishing);
        quotas.reactToFishing(where, who, fishCaught, fishRetained, hoursSpentFishing);
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
            Species species, Fisher seller, double biomass, double revenue) {
        mpaRules.reactToSale(species,seller,biomass,revenue);
        fishingSeason.reactToSale(species,seller,biomass,revenue);
        quotas.reactToSale(species,seller,biomass,revenue);
    }

    /**
     * returns a copy of the regulation, used defensively
     *
     * @return
     */
    @Override
    public Regulation makeCopy() {
        return new KitchenSinkRegulation(((TemporaryProtectedArea) mpaRules.makeCopy()),
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
        mpaRules.start(model,fisher);
        fishingSeason.start(model,fisher);
        quotas.start(model,fisher);
    }

    @Override
    public void turnOff(Fisher fisher) {
        mpaRules.turnOff(fisher);
        fishingSeason.turnOff(fisher);
        quotas.turnOff(fisher);
    }
}
