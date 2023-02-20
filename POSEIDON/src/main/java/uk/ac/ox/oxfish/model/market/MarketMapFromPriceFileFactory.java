package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Map;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toMap;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class MarketMapFromPriceFileFactory implements AlgorithmFactory<MarketMap> {

    private InputPath priceFile;
    private int targetYear;
    private Supplier<SpeciesCodes> speciesCodesSupplier;

    public MarketMapFromPriceFileFactory(
        final Supplier<SpeciesCodes> speciesCodesSupplier,
        final InputPath priceFile,
        final int targetYear
    ) {
        this.speciesCodesSupplier = speciesCodesSupplier;
        this.priceFile = priceFile;
        this.targetYear = targetYear;
    }

    /**
     * Empty constructor needed to use as factory
     */
    @SuppressWarnings("unused")
    public MarketMapFromPriceFileFactory() {
    }

    public Supplier<SpeciesCodes> getSpeciesCodesSupplier() {
        return speciesCodesSupplier;
    }

    public void setSpeciesCodesSupplier(final Supplier<SpeciesCodes> speciesCodesSupplier) {
        this.speciesCodesSupplier = speciesCodesSupplier;
    }

    @SuppressWarnings("unused")
    public int getTargetYear() {
        return targetYear;
    }

    @SuppressWarnings("unused")
    public void setTargetYear(final int targetYear) {
        this.targetYear = targetYear;
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
        final Map<String, Double> prices = recordStream(priceFile.get())
            .filter(
                r -> r.getInt("year") == targetYear
            )
            .collect(toMap(
                r -> r.getString("species"),
                r -> r.getDouble("price") / 1000.0 // convert price / tonne to price / kg
            ));
        final GlobalBiology globalBiology = fishState.getBiology();
        final MarketMap marketMap = new MarketMap(globalBiology);
        final SpeciesCodes speciesCodes = speciesCodesSupplier.get();
        globalBiology.getSpecies().forEach(species -> {
            final String speciesCode = speciesCodes.getSpeciesCode(species.getName());
            marketMap.addMarket(species, new FixedPriceMarket(prices.get(speciesCode)));
        });
        return marketMap;
    }

}
