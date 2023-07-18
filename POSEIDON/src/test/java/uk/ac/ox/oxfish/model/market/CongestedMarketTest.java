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

package uk.ac.ox.oxfish.model.market;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CongestedMarketTest {


    @Test
    public void marketGetCongested() throws Exception {

        CongestedMarket market = new CongestedMarket(100, 10, 0.01, 0);
        Species species = mock(Species.class);

        market.setSpecies(species);
        market.start(mock(FishState.class));
        //sell 10, the revenue ought to be 100
        Hold hold = mock(Hold.class);
        when(hold.getWeightOfCatchInHold(any())).thenReturn(10d);
        TradeInfo tradeInfo = market.sellFish(hold, mock(Fisher.class), new Anarchy(), mock(FishState.class),
            species
        );
        Assert.assertEquals(tradeInfo.getMoneyExchanged(), 100, .001);
        //sell 90, again the price ought to be steady at 10
        when(hold.getWeightOfCatchInHold(any())).thenReturn(90d);
        tradeInfo = market.sellFish(hold, mock(Fisher.class), new Anarchy(), mock(FishState.class), species);
        Assert.assertEquals(tradeInfo.getMoneyExchanged(), 900, .001);

        //now do it once more, this time it pays less
        when(hold.getWeightOfCatchInHold(any())).thenReturn(100d);

        tradeInfo = market.sellFish(hold, mock(Fisher.class), new Anarchy(), mock(FishState.class), species);
        Assert.assertEquals(tradeInfo.getMoneyExchanged(), 900, .001);
        Assert.assertEquals(market.getMarginalPrice(), 9, .0001d);


    }


    @Test
    public void marketGetDecongested() {
        CongestedMarket market = new CongestedMarket(100, 10, 0.01, 150);
        Species species = mock(Species.class);
        market.setSpecies(species);
        market.start(mock(FishState.class));

        //decongested
        Assert.assertEquals(market.getMarginalPrice(), 10, .0001d);

        Hold hold = mock(Hold.class);
        when(hold.getWeightOfCatchInHold(any())).thenReturn(200d);
        market.sellFish(hold, mock(Fisher.class), new Anarchy(), mock(FishState.class), species);

        //congested
        Assert.assertEquals(market.getMarginalPrice(), 9, .0001d);

        //drops by 150 each step
        market.step(mock(FishState.class));
        //decongested
        Assert.assertEquals(market.getMarginalPrice(), 10, .0001d);
        //recongested
        market.sellFish(hold, mock(Fisher.class), new Anarchy(), mock(FishState.class), species);
        Assert.assertEquals(market.getMarginalPrice(), 8.5, .0001d);


    }
}