package uk.ac.ox.oxfish.model.market.factory;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.gear.PeriodicUpdateGearStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.ConditionalMarket;
import uk.ac.ox.oxfish.model.market.NThresholdsMarket;
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
        final NThresholdsMarket nonPremium = nonPremiumMarket.apply(fishState);
        final NThresholdsMarket premium = nonPremiumMarket.apply(fishState);
        final double v = premiumInPercentage.apply(fishState.getRandom());

        //save prices to restore later
        if(premiumFirstBin) {
            premium.getPricePerSegment()[0] =
                    premium.getPricePerSegment()[0] * v;
        }
        if(premiumSecondBin) {

            premium.getPricePerSegment()[1] =
                    premium.getPricePerSegment()[1] * v;

        }
        if(premiumThirdBin) {

            premium.getPricePerSegment()[2] =
                    premium.getPricePerSegment()[2] * v;

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

    public AlgorithmFactory<NThresholdsMarket> getNonPremiumMarket() {
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
