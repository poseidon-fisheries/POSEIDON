/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.model.regs.policymakers;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.MarketMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SingleSpeciesBiomassTaxmanTest {


    @Test
    public void taxIsImposedCorrectly() throws Exception {

        Species species = mock(Species.class);

        Port port1 = mock(Port.class);
        Port port2 = mock(Port.class);
        MarketMap marketMap1 = mock(MarketMap.class);
        when(port1.getDefaultMarketMap()).thenReturn(marketMap1);
        MarketMap marketMap2 = mock(MarketMap.class);
        when(port2.getDefaultMarketMap()).thenReturn(marketMap2);

        when(marketMap1.getMarket(species)).thenReturn(new FixedPriceMarket(30));
        when(marketMap2.getMarket(species)).thenReturn(new FixedPriceMarket(10));


        SingleSpeciesBiomassTaxman taxman = new SingleSpeciesBiomassTaxman(
            species,
            5,
            100,
            true
        );

        FishState model = mock(FishState.class);
        when(model.getPorts()).thenReturn(Lists.newArrayList(port1, port2));
        //there is biomass of 101, no tax should be imposed
        when(model.getTotalBiomass(species)).thenReturn(101d);
        taxman.step(model);
        assertEquals(30, marketMap1.getMarket(species).getMarginalPrice(), .0001);
        assertEquals(10, marketMap2.getMarket(species).getMarginalPrice(), .0001);
        //there is biomass of 99, tax should be imposed!
        when(model.getTotalBiomass(species)).thenReturn(99d);
        taxman.step(model);
        assertEquals(25, marketMap1.getMarket(species).getMarginalPrice(), .0001);
        assertEquals(5, marketMap2.getMarket(species).getMarginalPrice(), .0001);
        //biomass drops to 98, the tax remains
        when(model.getTotalBiomass(species)).thenReturn(98d);
        taxman.step(model);
        assertEquals(25, marketMap1.getMarket(species).getMarginalPrice(), .0001);
        assertEquals(5, marketMap2.getMarket(species).getMarginalPrice(), .0001);
        //biomass goes back to 101, tax is taken away
        when(model.getTotalBiomass(species)).thenReturn(101d);
        taxman.step(model);
        assertEquals(30, marketMap1.getMarket(species).getMarginalPrice(), .0001);
        assertEquals(10, marketMap2.getMarket(species).getMarginalPrice(), .0001);
        //biomass goes to 102, no tax
        when(model.getTotalBiomass(species)).thenReturn(102d);
        taxman.step(model);
        assertEquals(30, marketMap1.getMarket(species).getMarginalPrice(), .0001);
        assertEquals(10, marketMap2.getMarket(species).getMarginalPrice(), .0001);

    }
}