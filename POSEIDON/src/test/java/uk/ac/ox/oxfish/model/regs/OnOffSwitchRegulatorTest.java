/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

import com.beust.jcommander.internal.Lists;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.fxcollections.ObservableList;

import java.util.LinkedList;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class OnOffSwitchRegulatorTest {


    @Test
    public void switchedOnOffCorrectly() {
        //basically even if the order is random, if a boat is not allowed in then it shouldn't block other boats from joining

        Fisher one = mock(Fisher.class);
        OffSwitchDecorator decorator1 = mock(OffSwitchDecorator.class);
        when(one.getRegulation()).thenReturn(decorator1);
        Fisher two = mock(Fisher.class);
        OffSwitchDecorator decorator2 = mock(OffSwitchDecorator.class);
        when(two.getRegulation()).thenReturn(decorator2);
        Fisher three = mock(Fisher.class);
        OffSwitchDecorator decorator3 = mock(OffSwitchDecorator.class);
        when(three.getRegulation()).thenReturn(decorator3);
        Fisher four = mock(Fisher.class);
        OffSwitchDecorator decorator4 = mock(OffSwitchDecorator.class);
        when(four.getRegulation()).thenReturn(decorator4);

        FishState state  = mock(FishState.class);
        ObservableList<Fisher> fishers = ObservableList.observableList(one, two, three, four);
        when(state.getFishers()).thenReturn(fishers);


        PermitAllocationPolicy onlyFirstFisher = mock(PermitAllocationPolicy.class);
        when(onlyFirstFisher.computeWhichFishersAreAllowed(fishers,state)).thenReturn(Lists.newArrayList(one));
        OnOffSwitchRegulator regulator = new OnOffSwitchRegulator(onlyFirstFisher, new LinkedList<>());
        regulator.start(state);
        //regulator now steps when immediately started
     //   regulator.step(state);

        verify(((OffSwitchDecorator) one.getRegulation()),times(1)).setTurnedOff(false);
        verify(((OffSwitchDecorator) two.getRegulation()),times(1)).setTurnedOff(true);
        verify(((OffSwitchDecorator) three.getRegulation()),times(1)).setTurnedOff(true);
        verify(((OffSwitchDecorator) four.getRegulation()),times(1)).setTurnedOff(true);





    }


    @Test
    public void switchedOnOffCorrectlyWithTags() {
        //basically even if the order is random, if a boat is not allowed in then it shouldn't block other boats from joining

        Fisher one = mock(Fisher.class);
        OffSwitchDecorator decorator1 = mock(OffSwitchDecorator.class);
        when(one.getRegulation()).thenReturn(decorator1);
        Fisher two = mock(Fisher.class);
        OffSwitchDecorator decorator2 = mock(OffSwitchDecorator.class);
        when(two.getRegulation()).thenReturn(decorator2);
        Fisher three = mock(Fisher.class);
        OffSwitchDecorator decorator3 = mock(OffSwitchDecorator.class);
        when(three.getRegulation()).thenReturn(decorator3);
        Fisher four = mock(Fisher.class);
        OffSwitchDecorator decorator4 = mock(OffSwitchDecorator.class);
        when(four.getRegulation()).thenReturn(decorator4);


        when(one.getTags()).thenReturn(Lists.newArrayList("okay"));
        when(two.getTags()).thenReturn(Lists.newArrayList("okay"));
        when(three.getTags()).thenReturn(Lists.newArrayList("okay"));
        when(one.getTags()).thenReturn(Lists.newArrayList("nope"));

        FishState state  = mock(FishState.class);
        ObservableList<Fisher> fishers = ObservableList.observableList(one, two, three, four);
        when(state.getFishers()).thenReturn(fishers);


        PermitAllocationPolicy onlyFirstFisher = mock(PermitAllocationPolicy.class);
        when(onlyFirstFisher.computeWhichFishersAreAllowed(anyList(),any())).thenReturn(Lists.newArrayList(one));
        OnOffSwitchRegulator regulator = new OnOffSwitchRegulator(onlyFirstFisher, Lists.newArrayList("okay"));
        regulator.start(state);
        regulator.step(state);



        //four is neither turned off nor on; it is completely ignored
        verify(((OffSwitchDecorator) four.getRegulation()),times(0)).setTurnedOff(true);
        verify(((OffSwitchDecorator) four.getRegulation()),times(0)).setTurnedOff(false);





    }
}