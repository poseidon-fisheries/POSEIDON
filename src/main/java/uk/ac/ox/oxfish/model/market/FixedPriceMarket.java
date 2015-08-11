package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * The simplest market, the price is constant no matter how much stuff gets sold every day
 * Created by carrknight on 5/3/15.
 */
public class FixedPriceMarket extends AbstractMarket {

    private double price;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
  //      Preconditions.checkArgument(price>=0); can be negative if it's a fine!
        this.price = price;
    }


    public FixedPriceMarket(double price) {
        this.price = price;
    }

    /**
     * the only method to implement for subclasses. Needs to actually do the trading and return the result
     *
     * @param biomass     the biomass caught by the seller
     * @param fisher      the seller
     * @param regulation the rules the seller abides to
     * @param state       the model
     * @return TradeInfo  results
     */
    @Override
    protected TradeInfo sellFishImplementation(
            double biomass, Fisher fisher, Regulation regulation, FishState state,
            Specie specie) {
        return Market.defaultMarketTransaction(biomass, fisher, regulation, state,
                                               biomassTraded -> biomassTraded *price,specie);
    }

}
