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

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ThresholdWeightPriceTest {


    @Test
    public void sellsAndNotifiesCorrectly() {

        //set up copied from the holdsize test
        Meristics first = new FromListMeristics(new double[]{10, 50, 100}, 2);
        Meristics second = new FromListMeristics(new double[]{100, 200}, 2);
        Species firstSpecies = new Species("first", first);
        Species secondSpecies = new Species("second", second);


        GlobalBiology bio = new GlobalBiology(firstSpecies, secondSpecies);


        Hold hold = new Hold(
            1000d,
            bio
        );

        //you catch 1000kg of species 1
        hold.load(
            new Catch(
                new double[]{0, 2, 3}, // 100 + 300
                new double[]{5, 0, 0}, // 500!
                firstSpecies,
                bio

            )
        );


        FlexibleAbundanceMarket market = new FlexibleAbundanceMarket(
            new ThresholdWeightPrice(30, 20, 100)
        );

        market.setSpecies(firstSpecies);
        market.start(mock(FishState.class));
        Regulation regulation = mock(Regulation.class);
        when(regulation.maximumBiomassSellable(
            any(),
            any(),
            any()
        )).thenReturn(1500d);

        //sell the fish
        Fisher fisher = mock(Fisher.class);
        market.sellFish(
            hold,
            fisher,
            regulation,
            mock(FishState.class),
            firstSpecies
        );
        verify(fisher).earn(
            (20 * 5 * 10 + 20 * 2 * 50 + 30 * 3 * 100)

        );


    }
}
