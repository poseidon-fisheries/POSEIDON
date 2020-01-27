package uk.ac.ox.oxfish.model.market.factory;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.gear.PeriodicUpdateGearStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.ConditionalMarket;
import uk.ac.ox.oxfish.model.market.ThreePricesMarket;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.Predicate;

public class ThreePricesWithPremium implements AlgorithmFactory<ConditionalMarket> {

    private boolean premiumFirstBin = false;

    private boolean premiumSecondBin = false;

    private boolean premiumThirdBin = false;

    private DoubleParameter premiumInPercentage =
            new FixedDoubleParameter(2);

    private ThreePricesMarketFactory nonPremiumMarket;

    private String tagNeededToAccessToPremium = PeriodicUpdateGearStrategy.tag + "_1";


    @Override
    public ConditionalMarket apply(FishState fishState) {
        final ThreePricesMarket nonPremium = nonPremiumMarket.apply(fishState);
        final ThreePricesMarket premium = nonPremiumMarket.apply(fishState);
        final double v = premiumInPercentage.apply(fishState.getRandom());

        //save prices to restore later
        if(premiumFirstBin) {
            premium.setPriceBelowThreshold(
                    premium.getPriceBelowThreshold() * v

            );
        }
        if(premiumSecondBin) {
            premium.setPriceBetweenThresholds(
                    premium.getPriceBetweenThresholds() * v

            );
        }
        if(premiumThirdBin) {
            premium.setPriceAboveThresholds(
                    premium.getPriceAboveThresholds() * v

            );
        }


        return new ConditionalMarket(
                nonPremium,
                premium,
                new Predicate<Fisher>() {
                    @Override
                    public boolean test(Fisher fisher) {
                        return fisher.getTags().contains(tagNeededToAccessToPremium);
                    }
                }
        );
    }

    public boolean isPremiumFirstBin() {
        return premiumFirstBin;
    }

    public void setPremiumFirstBin(boolean premiumFirstBin) {
        this.premiumFirstBin = premiumFirstBin;
    }

    public boolean isPremiumSecondBin() {
        return premiumSecondBin;
    }

    public void setPremiumSecondBin(boolean premiumSecondBin) {
        this.premiumSecondBin = premiumSecondBin;
    }

    public boolean isPremiumThirdBin() {
        return premiumThirdBin;
    }

    public void setPremiumThirdBin(boolean premiumThirdBin) {
        this.premiumThirdBin = premiumThirdBin;
    }

    public DoubleParameter getPremiumInPercentage() {
        return premiumInPercentage;
    }

    public void setPremiumInPercentage(DoubleParameter premiumInPercentage) {
        this.premiumInPercentage = premiumInPercentage;
    }

    public AlgorithmFactory<ThreePricesMarket> getNonPremiumMarket() {
        return nonPremiumMarket;
    }

    public void setNonPremiumMarket(ThreePricesMarketFactory nonPremiumMarket) {
        this.nonPremiumMarket = nonPremiumMarket;
    }

    public String getTagNeededToAccessToPremium() {
        return tagNeededToAccessToPremium;
    }

    public void setTagNeededToAccessToPremium(String tagNeededToAccessToPremium) {
        this.tagNeededToAccessToPremium = tagNeededToAccessToPremium;
    }



}
