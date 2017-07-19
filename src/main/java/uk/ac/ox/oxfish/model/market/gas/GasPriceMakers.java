package uk.ac.ox.oxfish.model.market.gas;

import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.factory.FixedPriceMarketFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * Created by carrknight on 7/18/17.
 */
public class GasPriceMakers {

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final LinkedHashMap<String,Supplier<AlgorithmFactory<? extends GasPriceMaker>>> CONSTRUCTORS =
            new LinkedHashMap<>();

    public static final LinkedHashMap<Class<? extends AlgorithmFactory>,String> NAMES = new LinkedHashMap<>();



    static {

        CONSTRUCTORS.put("Fixed Gas Price",
                         FixedGasFactory::new
        );
        NAMES.put(FixedGasFactory.class,"Fixed Gas Price");

        CONSTRUCTORS.put("Gas Price from File",
                         CsvTimeSeriesGasFactory::new
        );
        NAMES.put(CsvTimeSeriesGasFactory.class,"Gas Price from File");

    }
}
