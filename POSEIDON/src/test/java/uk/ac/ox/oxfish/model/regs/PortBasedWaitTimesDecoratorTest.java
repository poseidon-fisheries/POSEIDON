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
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PortBasedWaitTimesDecoratorTest {


    @Test
    public void stayAtPort() {

        Port port = new Port(
            "correct",
            mock(SeaTile.class),
            mock(MarketMap.class),
            100
        );

        Fisher fisher = mock(Fisher.class);

        HashMap<String, Integer> hoursToWaitPerPort = new HashMap<>();
        hoursToWaitPerPort.put("correct", 100);
        hoursToWaitPerPort.put("false", 50);
        PortBasedWaitTimesDecorator decorator = new PortBasedWaitTimesDecorator(
            new Anarchy(),
            hoursToWaitPerPort
        );


        when(fisher.isAtPortAndDocked()).thenReturn(false); //never stop somebody who is already out!
        assertTrue(decorator.allowedAtSea(fisher, mock(FishState.class)));

        when(fisher.isAtPortAndDocked()).thenReturn(true); //allow somebody who has waited for a long time
        when(fisher.getHomePort()).thenReturn(port);
        when(fisher.getHoursAtPort()).thenReturn(200d);
        assertTrue(decorator.allowedAtSea(fisher, mock(FishState.class)));


        //but do not allow if they haven't waited enough!
        when(fisher.getHoursAtPort()).thenReturn(20d);
        assertFalse(decorator.allowedAtSea(fisher, mock(FishState.class)));

        //if the decorator says no, then it's no
        Regulation decorated = mock(Regulation.class);
        when(decorated.allowedAtSea(any(), any())).thenReturn(false);
        decorator = new PortBasedWaitTimesDecorator(
            decorated,
            hoursToWaitPerPort
        );
        when(fisher.getHoursAtPort()).thenReturn(200d);
        assertFalse(decorator.allowedAtSea(fisher, mock(FishState.class)));


    }
}