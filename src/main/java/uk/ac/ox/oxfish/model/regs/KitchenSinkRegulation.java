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


    private final ProtectedAreasOnly mpaRules;

    private final FishingSeason fishingSeason;

    private final QuotaPerSpecieRegulation quotas;

    public KitchenSinkRegulation(
            ProtectedAreasOnly mpaRules, FishingSeason fishingSeason,
            QuotaPerSpecieRegulation quotas) {
        this.mpaRules = mpaRules;
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
                        fishingSeason.canFishHere(agent,tile,model) &&
                        quotas.canFishHere(agent,tile,model);

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
     * tell the regulation object this much has been caught
     *
     * @param fishCaught catch object
     */
    @Override
    public void reactToCatch(Catch fishCaught) {
        mpaRules.reactToCatch(fishCaught);
        fishingSeason.reactToCatch(fishCaught);
        quotas.reactToCatch(fishCaught);
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
        return new KitchenSinkRegulation(((ProtectedAreasOnly) mpaRules.makeCopy()),
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
}
