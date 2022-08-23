package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.SpeciesCodeAware;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Path;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.*;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class YearlyMarketMapFromPriceFileFactory
    implements AlgorithmFactory<MarketMap>, SpeciesCodeAware {

    private Path priceFilePath;
    private SpeciesCodes speciesCodes;

    public YearlyMarketMapFromPriceFileFactory(Path priceFilePath) {
        this.priceFilePath = priceFilePath;
    }

    /**
     * Empty constructor needed to use as factory
     */
    @SuppressWarnings("unused")
    public YearlyMarketMapFromPriceFileFactory() {
    }

    @Override
    public SpeciesCodes getSpeciesCodes() {
        return speciesCodes;
    }

    @Override
    public void setSpeciesCodes(SpeciesCodes speciesCodes) {
        this.speciesCodes = speciesCodes;
    }

    @SuppressWarnings("unused")
    public Path getPriceFilePath() {
        return priceFilePath;
    }

    @SuppressWarnings("unused")
    public void setPriceFilePath(Path priceFilePath) {
        this.priceFilePath = priceFilePath;
    }

    @Override
    public MarketMap apply(FishState fishState) {
        checkNotNull(speciesCodes, "need to call setSpeciesCodes() before using");
        GlobalBiology globalBiology = fishState.getBiology();
        Map<Species, FixedYearlyPricesBiomassMarket> prices =
            recordStream(priceFilePath).collect(
                groupingBy(
                    r -> globalBiology.getSpecie(speciesCodes.getSpeciesName(r.getString("species"))),
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
