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

package uk.ac.ox.oxfish.fisher;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.market.MarketMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class PortTest {


    @Test
    public void registersCorrectly() throws Exception {

        final SeaTile location = mock(SeaTile.class);
        final Port port = new Port("Port 0", location, mock(MarketMap.class), 0);

        final Fisher one = mock(Fisher.class);
        when(one.getLocation()).thenReturn(location);
        final Fisher two = mock(Fisher.class);
        when(two.getLocation()).thenReturn(location);

        //initially neither fisher is at port
        Assertions.assertFalse(port.isDocked(one));
        Assertions.assertFalse(port.isDocked(two));
        //one enters the port
        port.dock(one);
        Assertions.assertTrue(port.isDocked(one));
        Assertions.assertFalse(port.isDocked(two));
        //two enters port
        port.dock(two);
        Assertions.assertTrue(port.isDocked(one));
        Assertions.assertTrue(port.isDocked(two));
        //two exits port
        port.depart(two);
        Assertions.assertTrue(port.isDocked(one));
        Assertions.assertFalse(port.isDocked(two));
    }

    public void wrongLocationThrowsException() {
        Assertions.assertThrows(RuntimeException.class, () -> {
            final SeaTile location1 = mock(SeaTile.class);
            final SeaTile location2 = mock(SeaTile.class);
            final Port port = new Port("Port 0", location1, mock(MarketMap.class), 0);

            final Fisher one = mock(Fisher.class);
            when(one.getLocation()).thenReturn(location2);
            //one is not sharing the sea-tile with port
            port.dock(one);
        });
    }

    public void dockingTwiceIsNotAllowd() {
        Assertions.assertThrows(IllegalStateException.class, () -> {

            final SeaTile location = mock(SeaTile.class);
            final Port port = new Port("Port 0", location, mock(MarketMap.class), 0);

            final Fisher one = mock(Fisher.class);
            when(one.getLocation()).thenReturn(location);
            port.dock(one);
            port.dock(one);
        });
    }

    public void undockingWithoutBeingDockedIsNotAllowed() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            final SeaTile location = mock(SeaTile.class);
            final Port port = new Port("Port 0", location, mock(MarketMap.class), 0);

            final Fisher one = mock(Fisher.class);
            when(one.getLocation()).thenReturn(location);
            port.depart(one);
        });
    }

}