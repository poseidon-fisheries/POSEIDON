/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
