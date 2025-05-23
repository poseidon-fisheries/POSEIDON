/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2018-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.market.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.MarketProxy;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.util.LinkedHashMap;
import java.util.Map;

public class SpeciesMarketMappedFactory implements AlgorithmFactory<MarketProxy> {

    private LinkedHashMap<String, AlgorithmFactory<? extends Market>> markets = new LinkedHashMap<>();


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public MarketProxy apply(final FishState fishState) {
        return new MarketProxy(new LinkedHashMap<>(markets));
    }

    /**
     * Getter for property 'markets'.
     *
     * @return Value for property 'markets'.
     */
    public LinkedHashMap<String, AlgorithmFactory<? extends Market>> getMarkets() {
        return markets;
    }

    /**
     * Setter for property 'markets'.
     *
     * @param markets Value to set for property 'markets'.
     */
    @SuppressWarnings("unchecked")
    public void setMarkets(
        final LinkedHashMap<String, ?> markets
    ) {

        //useless cast, but it deals with YAML quirks
        final LinkedHashMap<String, AlgorithmFactory<? extends Market>> real = new LinkedHashMap<>();

        final FishYAML yaml = new FishYAML();

        //force it to go through YAML
        for (final Map.Entry<String, ?> entry : markets.entrySet()) {
            final Object factory = entry.getValue();

            final AlgorithmFactory<? extends Market> recast = (AlgorithmFactory<? extends Market>)
                yaml.loadAs(yaml.dump(factory), AlgorithmFactory.class);
            real.put(entry.getKey(), recast);

        }


        this.markets = real;
    }


}
