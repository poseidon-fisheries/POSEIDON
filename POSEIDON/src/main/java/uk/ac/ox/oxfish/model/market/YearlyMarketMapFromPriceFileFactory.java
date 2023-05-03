package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Map;
import java.util.function.Supplier;

import static java.util.stream.Collectors.*;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class YearlyMarketMapFromPriceFileFactory
    implements AlgorithmFactory<MarketMap> {

    private InputPath priceFile;

    public YearlyMarketMapFromPriceFileFactory(
        final InputPath priceFile,
        final Supplier<SpeciesCodes> speciesCodesSupplier
    ) {
        this.priceFile = priceFile;
        this.speciesCodesSupplier = speciesCodesSupplier;
    }

    private Supplier<SpeciesCodes> speciesCodesSupplier;

    /**
     * Empty constructor needed to use as factory
     */
    @SuppressWarnings("unused")
    public YearlyMarketMapFromPriceFileFactory() {
    }

    public Supplier<SpeciesCodes> getSpeciesCodesSupplier() {
        return speciesCodesSupplier;
    }

    public void setSpeciesCodesSupplier(final Supplier<SpeciesCodes> speciesCodesSupplier) {
        this.speciesCodesSupplier = speciesCodesSupplier;
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
        final SpeciesCodes speciesCodes = speciesCodesSupplier.get();
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
