package uk.ac.ox.oxfish.model.market.factory;

import uk.ac.ox.oxfish.fisher.strategies.gear.PeriodicUpdateGearStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.ConditionalMarket;
import uk.ac.ox.oxfish.model.market.NThresholdsMarket;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class ThreePricesWithPremium implements AlgorithmFactory<ConditionalMarket> {

    private boolean premiumFirstBin = false;

    private boolean premiumSecondBin = false;

    private boolean premiumThirdBin = false;

    private DoubleParameter premiumInPercentage =
        new FixedDoubleParameter(2);

    private ThreePricesMarketFactory nonPremiumMarket;

    private String tagNeededToAccessToPremium = PeriodicUpdateGearStrategy.tag + "_1";


    @Override
    public ConditionalMarket apply(final FishState fishState) {
        final NThresholdsMarket nonPremium = nonPremiumMarket.apply(fishState);
        final NThresholdsMarket premium = nonPremiumMarket.apply(fishState);
        final double v = premiumInPercentage.applyAsDouble(fishState.getRandom());

        //save prices to restore later
        if (premiumFirstBin) {
            premium.getPricePerSegment()[0] =
                premium.getPricePerSegment()[0] * v;
        }
        if (premiumSecondBin) {

            premium.getPricePerSegment()[1] =
                premium.getPricePerSegment()[1] * v;

        }
        if (premiumThirdBin) {

            premium.getPricePerSegment()[2] =
                premium.getPricePerSegment()[2] * v;

        }


        return new ConditionalMarket(
            nonPremium,
            premium,
            fisher -> fisher.getTagsList().contains(tagNeededToAccessToPremium)
        );
    }

    public boolean isPremiumFirstBin() {
        return premiumFirstBin;
    }

    public void setPremiumFirstBin(final boolean premiumFirstBin) {
        this.premiumFirstBin = premiumFirstBin;
    }

    public boolean isPremiumSecondBin() {
        return premiumSecondBin;
    }

    public void setPremiumSecondBin(final boolean premiumSecondBin) {
        this.premiumSecondBin = premiumSecondBin;
    }

    public boolean isPremiumThirdBin() {
        return premiumThirdBin;
    }

    public void setPremiumThirdBin(final boolean premiumThirdBin) {
        this.premiumThirdBin = premiumThirdBin;
    }

    public DoubleParameter getPremiumInPercentage() {
        return premiumInPercentage;
    }

    public void setPremiumInPercentage(final DoubleParameter premiumInPercentage) {
        this.premiumInPercentage = premiumInPercentage;
    }

    public AlgorithmFactory<NThresholdsMarket> getNonPremiumMarket() {
        return nonPremiumMarket;
    }

    public void setNonPremiumMarket(final ThreePricesMarketFactory nonPremiumMarket) {
        this.nonPremiumMarket = nonPremiumMarket;
    }

    public String getTagNeededToAccessToPremium() {
        return tagNeededToAccessToPremium;
    }

    public void setTagNeededToAccessToPremium(final String tagNeededToAccessToPremium) {
        this.tagNeededToAccessToPremium = tagNeededToAccessToPremium;
    }


}
