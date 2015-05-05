package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulations;

import java.util.function.Function;

/**
 * The market for a specie of fish
 * Created by carrknight on 5/3/15.
 */
public interface Market {




    public void start(FishState state);

    /**
     * Sells the a specific amount of fish here
     * @param biomass pounds of fish sold
     * @param fisher the seller
     * @param regulations the regulation object the seller abides to
     * @param state the model
     */
    public TradeInfo sellFish(double biomass, Fisher fisher,
                         Regulations regulations, FishState state);


    /**
     * does some standard stuff relating to selling fish:
     * 1) finds out how much is actually legal to sell
     * 2) give fisher money
     * 3) tell regulations
     * notice that it doesn't tell fisher to clear its hold, that should be the fisher responsibility
     * @param biomass pounds of fish to sell
     * @param fisher the seller
     * @param regulations the regulation object the seller abides to
     * @param state the model
     * @param biomassToRevenue a function to find out how much money does the biomass sold actually command
     * @return biomass actually sold
     */
    static public TradeInfo defaultMarketTransaction(
            double biomass, Fisher fisher, Regulations regulations,
            FishState state, Function<Double, Double> biomassToRevenue, Specie specie
    )
    {

        //find out legal biomass sold
        double biomassActuallySellable = Math.min(biomass,
                                                  regulations.maximumBiomassSellable(fisher, specie, state));
        if(biomassActuallySellable <=0)
            return new TradeInfo(0,specie,0);


        double revenue = biomassToRevenue.apply(biomassActuallySellable);

        //give fisher the money
        fisher.earn(revenue);

        //tell regulations
        regulations.reactToSale(specie,biomassActuallySellable,revenue);

        //return biomass sellable
        return new TradeInfo(biomassActuallySellable,specie,revenue);

    }
}
