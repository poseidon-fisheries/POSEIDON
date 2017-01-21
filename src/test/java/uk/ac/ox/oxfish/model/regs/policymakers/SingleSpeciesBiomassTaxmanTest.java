package uk.ac.ox.oxfish.model.regs.policymakers;

import com.google.common.collect.Sets;
import org.junit.Test;
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



        SingleSpeciesBiomassTaxman taxman = new SingleSpeciesBiomassTaxman(species,
                                                                           5,
                                                                           100,
                                                                           true);

        FishState model = mock(FishState.class);
        when(model.getPorts()).thenReturn(Sets.newHashSet(port1,port2));
        //there is biomass of 101, no tax should be imposed
        when(model.getTotalBiomass(species)).thenReturn(101d);
        taxman.step(model);
        assertEquals(30,marketMap1.getMarket(species).getMarginalPrice(),.0001);
        assertEquals(10,marketMap2.getMarket(species).getMarginalPrice(),.0001);
        //there is biomass of 99, tax should be imposed!
        when(model.getTotalBiomass(species)).thenReturn(99d);
        taxman.step(model);
        assertEquals(25,marketMap1.getMarket(species).getMarginalPrice(),.0001);
        assertEquals(5,marketMap2.getMarket(species).getMarginalPrice(),.0001);
        //biomass drops to 98, the tax remains
        when(model.getTotalBiomass(species)).thenReturn(98d);
        taxman.step(model);
        assertEquals(25,marketMap1.getMarket(species).getMarginalPrice(),.0001);
        assertEquals(5,marketMap2.getMarket(species).getMarginalPrice(),.0001);
        //biomass goes back to 101, tax is taken away
        when(model.getTotalBiomass(species)).thenReturn(101d);
        taxman.step(model);
        assertEquals(30,marketMap1.getMarket(species).getMarginalPrice(),.0001);
        assertEquals(10,marketMap2.getMarket(species).getMarginalPrice(),.0001);
        //biomass goes to 102, no tax
        when(model.getTotalBiomass(species)).thenReturn(102d);
        taxman.step(model);
        assertEquals(30,marketMap1.getMarket(species).getMarginalPrice(),.0001);
        assertEquals(10,marketMap2.getMarket(species).getMarginalPrice(),.0001);

    }
}