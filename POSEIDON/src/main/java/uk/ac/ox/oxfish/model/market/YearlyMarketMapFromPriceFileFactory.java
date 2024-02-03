package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

import java.util.Map;

import static java.util.stream.Collectors.*;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

public class YearlyMarketMapFromPriceFileFactory
    implements AlgorithmFactory<MarketMap> {

    private InputPath priceFile;

    public YearlyMarketMapFromPriceFileFactory(
        final InputPath priceFile
    ) {
        this.priceFile = priceFile;
    }

    /**
     * Empty constructor needed to use as factory
     */
    @SuppressWarnings("unused")
    public YearlyMarketMapFromPriceFileFactory() {
    }

    @SuppressWarnings("unused")
    public InputPath getPriceFile() {
        return priceFile;
    }

    @SuppressWarnings("unused")
    public void setPriceFile(final InputPath priceFile) {
        this.priceFile = priceFile;
    }

    @Override
    public MarketMap apply(final FishState fishState) {
        final GlobalBiology globalBiology = fishState.getBiology();
        final Map<Species, FixedYearlyPricesBiomassMarket> prices =
            recordStream(priceFile.get()).collect(
                groupingBy(
                    r -> globalBiology.getSpeciesByCode(r.getString("species")),
                    collectingAndThen(toMap(
                        r -> r.getInt("year"),
                        r -> r.getDouble("price") / 1000.0  // convert price / tonne to price / kg
                    ), FixedYearlyPricesBiomassMarket::new)
                )
            );
        final MarketMap marketMap = new MarketMap(globalBiology);
        prices.forEach(marketMap::addMarket);
        return marketMap;
    }

}
