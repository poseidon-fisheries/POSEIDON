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

package uk.ac.ox.oxfish.fisher.equipment.gear;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.ConstantLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.mockito.Mockito.*;

public class DelayGearDecoratorTest {


    @Test
    public void tenHourDelay() {


        ConstantLocalBiology biology = new ConstantLocalBiology(1000);
        FixedProportionGear delegate = spy(new FixedProportionGear(.5));
        DelayGearDecorator gear = new DelayGearDecorator(
            delegate,
            10
        );

        for (int hour = 0; hour < 9; hour++) {
            Catch catchMade = gear.fish(
                mock(Fisher.class),
                biology,
                mock(SeaTile.class),
                1,
                new GlobalBiology(new Species("fake"))
            );
            //should not have called the original proportion gear at all
            verify(delegate, never()).fish(any(), any(), any(), anyInt(), any());
            Assertions.assertEquals(catchMade.getTotalWeight(), 0, .001);
        }

        //10th hour it should catch!
        Catch catchMade = gear.fish(
            mock(Fisher.class),
            biology,
            mock(SeaTile.class),
            1,
            new GlobalBiology(new Species("fake"))
        );
        //should not have called the original proportion gear at all
        verify(delegate, times(1)).fish(any(), any(), any(), anyInt(), any());
        //should have caught 500kg!
        Assertions.assertEquals(catchMade.getTotalWeight(), 500, .001);
    }

    @Test
    public void fishThreeTimesInTwentyFoursHours() {


        Gear delegate = mock(Gear.class);
        DelayGearDecorator gear = new DelayGearDecorator(
            delegate,
            8
        );

        for (int hour = 0; hour < 24; hour++) {
            Catch catchMade = gear.fish(
                mock(Fisher.class),
                mock(LocalBiology.class),
                mock(SeaTile.class),
                1,
                new GlobalBiology(new Species("fake"))
            );

        }

        verify(delegate, times(3)).fish(any(), any(), any(), anyInt(), any());

    }
}
