package uk.ac.ox.oxfish.model.market.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Arrays;
import java.util.function.ToDoubleFunction;

/**
 * loops through an array of prices to create fixed price markets
 * Created by carrknight on 8/31/16.
 */
public class ArrayFixedPriceMarket implements AlgorithmFactory<FixedPriceMarket> {


    /**
     * price is 10 for 2 species
     */
    private String prices = "10,10";


    /**
     * when first called the constructor
     */
    private double[] pricesAsNumbers = null;

    private int index = 0;

    public ArrayFixedPriceMarket() {
    }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public FixedPriceMarket apply(FishState fishState) {

        if(pricesAsNumbers == null)
            pricesAsNumbers = Arrays.stream(prices.split(",")).
                    mapToDouble(Double::parseDouble).toArray();

        FixedPriceMarket market = new FixedPriceMarket(pricesAsNumbers[index]);
        index++;
        return market;
    }

    /**
     * Getter for property 'prices'.
     *
     * @return Value for property 'prices'.
     */
    public String getPrices() {
        return prices;
    }

    /**
     * Setter for property 'prices'.
     *
     * @param prices Value to set for property 'prices'.
     */
    public void setPrices(String prices) {
        this.prices = prices;
    }
}
