package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputFile;
import uk.ac.ox.oxfish.model.scenario.SpeciesCodeAware;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.*;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class YearlyMarketMapFromPriceFileFactory
    implements AlgorithmFactory<MarketMap>, SpeciesCodeAware {

    private InputFile priceFile;
    private SpeciesCodes speciesCodes;

    public YearlyMarketMapFromPriceFileFactory(final InputFile priceFile) {
        this.priceFile = priceFile;
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
    public void setSpeciesCodes(final SpeciesCodes speciesCodes) {
        this.speciesCodes = speciesCodes;
    }

    @SuppressWarnings("unused")
    public InputFile getPriceFile() {
        return priceFile;
    }

    @SuppressWarnings("unused")
    public void setPriceFile(final InputFile priceFile) {
        this.priceFile = priceFile;
    }

    @Override
    public MarketMap apply(final FishState fishState) {
        checkNotNull(speciesCodes, "need to call setSpeciesCodes() before using");
        final GlobalBiology globalBiology = fishState.getBiology();
        final Map<Species, FixedYearlyPricesBiomassMarket> prices =
            recordStream(priceFile.get()).collect(
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
