package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Path;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

public class MarketMapFromPriceFileFactory implements AlgorithmFactory<MarketMap> {

    private Path priceFilePath;
    private int targetYear;
    private SpeciesCodes speciesCodes;

    public MarketMapFromPriceFileFactory(Path priceFilePath, int targetYear) {
        this.priceFilePath = priceFilePath;
        this.targetYear = targetYear;
    }

    /**
     * Empty constructor needed to use as factory
     */
    @SuppressWarnings("unused")
    public MarketMapFromPriceFileFactory() { }

    public SpeciesCodes getSpeciesCodes() {
        return speciesCodes;
    }

    public void setSpeciesCodes(SpeciesCodes speciesCodes) {
        this.speciesCodes = speciesCodes;
    }

    @SuppressWarnings("unused")
    public int getTargetYear() {
        return targetYear;
    }

    @SuppressWarnings("unused")
    public void setTargetYear(int targetYear) {
        this.targetYear = targetYear;
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
        Map<String, Double> prices = parseAllRecords(priceFilePath).stream()
            .filter(
                r -> r.getInt("year") == targetYear
            )
            .collect(toMap(
                r -> r.getString("species"),
                r -> r.getDouble("price") / 1000.0 // convert price / tonne to price / kg
            ));
        GlobalBiology globalBiology = fishState.getBiology();
        final MarketMap marketMap = new MarketMap(globalBiology);
        globalBiology.getSpecies().forEach(species -> {
            final String speciesCode = speciesCodes.getSpeciesCode(species.getName());
            marketMap.addMarket(species, new FixedPriceMarket(prices.get(speciesCode)));
        });
        return marketMap;
    }

}
