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
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

public class MarketMapFromPriceFileFactory implements AlgorithmFactory<MarketMap> {

    private InputPath priceFile;
    private IntegerParameter targetYear;

    public MarketMapFromPriceFileFactory(
        final InputPath priceFile,
        final IntegerParameter targetYear
    ) {
        this.priceFile = priceFile;
        this.targetYear = targetYear;
    }

    /**
     * Empty constructor needed to use as factory
     */
    @SuppressWarnings("unused")
    public MarketMapFromPriceFileFactory() {
    }

    @SuppressWarnings("unused")
    public IntegerParameter getTargetYear() {
        return targetYear;
    }

    @SuppressWarnings("unused")
    public void setTargetYear(final IntegerParameter targetYear) {
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
                r -> Objects.equals(r.getInt("year"), targetYear.getValue())
            )
            .collect(toMap(
                r -> r.getString("species"),
                r -> r.getDouble("price") / 1000.0 // convert price / tonne to price / kg
            ));
        final GlobalBiology globalBiology = fishState.getBiology();
        final MarketMap marketMap = new MarketMap(globalBiology);
        globalBiology.getSpecies().forEach(species ->
            marketMap.addMarket(species, new FixedPriceMarket(prices.get(species.getCode())))
        );
        return marketMap;
    }

}
