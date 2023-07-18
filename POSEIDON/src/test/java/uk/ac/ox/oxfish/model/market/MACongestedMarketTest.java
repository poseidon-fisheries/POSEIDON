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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;

import static org.mockito.Mockito.mock;


public class MACongestedMarketTest {


    @Test
    public void congested() throws Exception {

        MACongestedMarket oldman = new MACongestedMarket(10, 1, 10);
        Species species = mock(Species.class);
        oldman.setSpecies(species);

        oldman.start(mock(FishState.class));
        Assertions.assertEquals(10, oldman.getMarginalPrice(), .0001d);

        TradeInfo tradeInfo = oldman.sellFishImplementation(2, mock(Fisher.class), new Anarchy(),
            mock(FishState.class), species
        );
        Assertions.assertEquals(2 * 10, tradeInfo.getMoneyExchanged(), .0001);
        Assertions.assertEquals(2, tradeInfo.getBiomassTraded(), .0001);

        //trading on the same day there is no hit to the congestion
        tradeInfo = oldman.sellFishImplementation(2, mock(Fisher.class), new Anarchy(),
            mock(FishState.class), species
        );
        Assertions.assertEquals(2 * 10, tradeInfo.getMoneyExchanged(), .0001);
        Assertions.assertEquals(2, tradeInfo.getBiomassTraded(), .0001);

        oldman.step(mock(FishState.class));
        //now the price ought to be 6!
        Assertions.assertEquals(6, oldman.getMarginalPrice(), .0001d);

        //step it again and it should be 8
        oldman.step(mock(FishState.class));
        Assertions.assertEquals(8, oldman.getMarginalPrice(), .0001d);
        tradeInfo = oldman.sellFishImplementation(100, mock(Fisher.class), new Anarchy(),
            mock(FishState.class), species
        );
        Assertions.assertEquals(100 * 8, tradeInfo.getMoneyExchanged(), .0001);
        Assertions.assertEquals(100, tradeInfo.getBiomassTraded(), .0001);


        //now the price should be 0
        oldman.step(mock(FishState.class));
        Assertions.assertEquals(0, oldman.getMarginalPrice(), .0001d);

    }
}