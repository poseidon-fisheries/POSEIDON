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

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 7/4/17.
 */
public class ThreePricesMarketTest {


    @Test
    public void sellsAndNotifiesCorrectly() throws Exception {

        //set up copied from the holdsize test
        StockAssessmentCaliforniaMeristics first = mock(StockAssessmentCaliforniaMeristics.class);
        StockAssessmentCaliforniaMeristics second = mock(StockAssessmentCaliforniaMeristics.class);
        Species firstSpecies = new Species("first",first);
        Species secondSpecies = new Species("second",second);


        GlobalBiology bio = new GlobalBiology(firstSpecies, secondSpecies);

        when(first.getMaxAge()).thenReturn(2);
        when(second.getMaxAge()).thenReturn(1);



        when(first.getWeight(anyInt(),anyInt())).thenReturn(100d);
        when(second.getWeight(anyInt(),anyInt())).thenReturn(100d);


        Hold hold = new Hold(1000d,
                             bio);

        //you catch 1000kg of species 2
        hold.load(
                new Catch(
                        new double[]{0,2,3},
                        new double[]{5,0,0},
                        firstSpecies,
                        bio

                )
        );


        ThreePricesMarket market = new ThreePricesMarket(
                0,1,
                10,
                20,
                30
        );
        market.setSpecies(firstSpecies);
        market.start(mock(FishState.class));
        Regulation regulation = mock(Regulation.class);
        when(regulation.maximumBiomassSellable(any(),
                                               any(),
                                               any())).thenReturn(1500d);

        //sell the fish
        Fisher fisher = mock(Fisher.class);
        market.sellFish(hold,
                        fisher,
                        regulation,
                        mock(FishState.class),
                        firstSpecies);
        verify(fisher).earn(
                (10*5*100d+20*2*100d+30*3*100d)

        );




    }

    @Test
    public void regulationHalves() throws Exception {

        //set up copied from the holdsize test
        StockAssessmentCaliforniaMeristics first = mock(StockAssessmentCaliforniaMeristics.class);
        StockAssessmentCaliforniaMeristics second = mock(StockAssessmentCaliforniaMeristics.class);
        Species firstSpecies = new Species("first",first);
        Species secondSpecies = new Species("second",second);


        GlobalBiology bio = new GlobalBiology(firstSpecies, secondSpecies);

        when(first.getMaxAge()).thenReturn(2);
        when(second.getMaxAge()).thenReturn(1);

        when(first.getWeight(anyInt(),anyInt())).thenReturn(100d);
        when(second.getWeight(anyInt(),anyInt())).thenReturn(100d);


        Hold hold = new Hold(1000d,
                             bio);

        //you catch 1000kg of species 2
        //but regulations will only allow to sell 500kg
        hold.load(
                new Catch(
                        new double[]{0,2,3},
                        new double[]{5,0,0},
                        firstSpecies,
                        bio

                )
        );


        ThreePricesMarket market = new ThreePricesMarket(
                0,1,
                10,
                20,
                30
        );
        market.setSpecies(firstSpecies);
        market.start(mock(FishState.class));
        Regulation regulation = mock(Regulation.class);
        when(regulation.maximumBiomassSellable(any(),
                                               any(),
                                               any())).thenReturn(500d);

        //sell the fish
        Fisher fisher = mock(Fisher.class);
        market.sellFish(hold,
                        fisher,
                        regulation,
                        mock(FishState.class),
                        firstSpecies);
        verify(fisher).earn(
                //you only sold half of the total value
                (10*5*100d+20*2*100d+30*3*100d)/2d

        );




    }
}