/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.model.regs;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TriggerRegulationTest {

    @Test
    public void switchesCorrectly() {


        Regulation businessAsUsual = mock(Regulation.class);
        Regulation emergency = mock(Regulation.class);
        TriggerRegulation trigger = new TriggerRegulation(
                10,
                100,
                "Test Indicator",
                businessAsUsual,
                emergency
        );
        FishState model = mock(FishState.class);
        when(model.getLatestYearlyObservation("Test Indicator")).thenReturn(100d);
        //at the start, it should be set to business as usual

        trigger.allowedAtSea(mock(Fisher.class),model);
        verify(businessAsUsual,times(1)).allowedAtSea(any(),any());
        verify(emergency,times(0)).allowedAtSea(any(),any());

        //stepping it, should stay with business as usual
        trigger.step(model);
        trigger.allowedAtSea(mock(Fisher.class),model);
        verify(businessAsUsual,times(2)).allowedAtSea(any(),any());
        verify(emergency,times(0)).allowedAtSea(any(),any());

        //indicator drops, but not enough for an emergency
        when(model.getLatestYearlyObservation("Test Indicator")).thenReturn(50d);
        trigger.step(model);
        trigger.allowedAtSea(mock(Fisher.class),model);
        verify(businessAsUsual,times(3)).allowedAtSea(any(),any());
        verify(emergency,times(0)).allowedAtSea(any(),any());

        //emergency ought to kick in now
        when(model.getLatestYearlyObservation("Test Indicator")).thenReturn(5d);
        trigger.step(model);
        trigger.allowedAtSea(mock(Fisher.class),model);
        verify(businessAsUsual,times(3)).allowedAtSea(any(),any());
        verify(emergency,times(1)).allowedAtSea(any(),any());

        //increasing above the low threshold is not enough to remove emergency
        when(model.getLatestYearlyObservation("Test Indicator")).thenReturn(50d);
        trigger.step(model);
        trigger.allowedAtSea(mock(Fisher.class),model);
        verify(businessAsUsual,times(3)).allowedAtSea(any(),any());
        verify(emergency,times(2)).allowedAtSea(any(),any());

        //will return to business as usual when the indicator is back to above the high threshold
        when(model.getLatestYearlyObservation("Test Indicator")).thenReturn(200d);
        trigger.step(model);
        trigger.allowedAtSea(mock(Fisher.class),model);
        verify(businessAsUsual,times(4)).allowedAtSea(any(),any());
        verify(emergency,times(2)).allowedAtSea(any(),any());
    }

}