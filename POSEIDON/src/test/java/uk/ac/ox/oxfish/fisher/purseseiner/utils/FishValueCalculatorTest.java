/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.MarketMap;

import static org.junit.Assert.assertEquals;
import static uk.ac.ox.oxfish.biology.GlobalBiology.genericListOfSpecies;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class FishValueCalculatorTest {

    @Test
    public void test() {
        final GlobalBiology biology = genericListOfSpecies(2);
        final MarketMap marketMap = new MarketMap(biology);
        marketMap.addMarket(biology.getSpecie(0), new FixedPriceMarket(1));
        marketMap.addMarket(biology.getSpecie(1), new FixedPriceMarket(2));

        final FishValueCalculator fishValueCalculator = new ReliableFishValueCalculator(biology);


        assertEquals(
            5.0,
            fishValueCalculator.valueOf(
                new BiomassLocalBiology(new double[]{1, 2}, new double[]{2, 2}),
                marketMap.getPrices()
            ),
            EPSILON
        );
        assertEquals(
            11.0,
            fishValueCalculator.valueOf(new Catch(new double[]{3, 4}), marketMap.getPrices()),
            EPSILON
        );

    }

}