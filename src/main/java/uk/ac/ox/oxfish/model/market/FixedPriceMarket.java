package uk.ac.ox.oxfish.model.market;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulations;

import java.util.function.Function;

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
        Preconditions.checkArgument(price>=0);
        this.price = price;
    }


    public FixedPriceMarket(Specie specie, double price) {
        super(specie);
        Preconditions.checkArgument(price>=0);
        this.price = price;
    }

    /**
     * the only method to implement for subclasses. Needs to actually do the trading and return the result
     *
     * @param biomass     the biomass caught by the seller
     * @param fisher      the seller
     * @param regulations the rules the seller abides to
     * @param state       the model
     * @return TradeInfo  results
     */
    @Override
    protected TradeInfo sellFishImplementation(
            double biomass, Fisher fisher, Regulations regulations, FishState state) {
        return Market.defaultMarketTransaction(biomass, fisher, regulations, state,
                                               biomassTraded -> biomassTraded *price,getSpecie());
    }
}
