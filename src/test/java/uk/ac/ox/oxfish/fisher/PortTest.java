package uk.ac.ox.oxfish.fisher;

import org.junit.Test;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 4/11/15.
 */
public class PortTest {


    @Test
    public void registersCorrectly() throws Exception {

        SeaTile location = mock(SeaTile.class);
        Port port = new Port(location);

        Fisher one = mock(Fisher.class); when(one.getLocation()).thenReturn(location);
        Fisher two = mock(Fisher.class);when(two.getLocation()).thenReturn(location);

        //initially neither fisher is at port
        assertFalse(port.isDocked(one));
        assertFalse(port.isDocked(two));
        //one enters the port
        port.dock(one);
        assertTrue(port.isDocked(one));
        assertFalse(port.isDocked(two));
        //two enters port
        port.dock(two);
        assertTrue(port.isDocked(one));
        assertTrue(port.isDocked(two));
        //two exits port
        port.depart(two);
        assertTrue(port.isDocked(one));
        assertFalse(port.isDocked(two));
    }

    @Test(expected=RuntimeException.class)
    public void wrongLocationThrowsException()
    {
        SeaTile location1 = mock(SeaTile.class);
        SeaTile location2 = mock(SeaTile.class);
        Port port = new Port(location1);

        Fisher one = mock(Fisher.class);when(one.getLocation()).thenReturn(location2);
        //one is not sharing the sea-tile with port
        port.dock(one);

    }

    @Test(expected=IllegalStateException.class)
    public void dockingTwiceIsNotAllowd()
    {
        SeaTile location = mock(SeaTile.class);
        Port port = new Port(location);

        Fisher one = mock(Fisher.class); when(one.getLocation()).thenReturn(location);
        port.dock(one);
        port.dock(one);
    }

    @Test(expected=IllegalStateException.class)
    public void undockingWithoutBeingDockedIsNotAllowed()
    {
        SeaTile location = mock(SeaTile.class);
        Port port = new Port(location);

        Fisher one = mock(Fisher.class); when(one.getLocation()).thenReturn(location);
        port.depart(one);
    }

}