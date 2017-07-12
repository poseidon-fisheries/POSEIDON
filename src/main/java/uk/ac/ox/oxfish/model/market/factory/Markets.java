package uk.ac.ox.oxfish.model.market.factory;

import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.ThreePricesMarket;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * Collections holding all the possible factories for markets
 * Created by carrknight on 8/11/15.
 */
public class Markets {

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final LinkedHashMap<String,Supplier<AlgorithmFactory<? extends Market>>> CONSTRUCTORS =
            new LinkedHashMap<>();

    public static final LinkedHashMap<Class<? extends AlgorithmFactory>,String> NAMES = new LinkedHashMap<>();


    static
    {

        CONSTRUCTORS.put("Fixed Price Market",
                         FixedPriceMarketFactory::new
        );
        NAMES.put(FixedPriceMarketFactory.class,"Fixed Price Market");

        CONSTRUCTORS.put("Fixed Price Market Array",
                         ArrayFixedPriceMarket::new
        );
        NAMES.put(ArrayFixedPriceMarket.class,"Fixed Price Market Array");

        CONSTRUCTORS.put("Congested Market",
                         CongestedMarketFactory::new
        );
        NAMES.put(CongestedMarketFactory.class,"Congested Market");

        CONSTRUCTORS.put("Moving Average Congested Market",
                         MACongestedMarketFactory::new
        );
        NAMES.put(MACongestedMarketFactory.class,
                  "Moving Average Congested Market");

        CONSTRUCTORS.put("Three Prices Market",
                         ThreePricesMarketFactory::new
        );
        NAMES.put(ThreePricesMarketFactory.class,
                  "Three Prices Market");


    }

}
