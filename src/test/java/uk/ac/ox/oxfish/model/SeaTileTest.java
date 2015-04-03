package uk.ac.ox.oxfish.model;

import org.junit.Test;
import sim.util.geo.MasonGeometry;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


public class SeaTileTest {


    @Test
    public void recognizesMPA() throws Exception {

        MasonGeometry mpa = mock(MasonGeometry.class);
        SeaTile tile = new SeaTile(0,0,0);
        tile.setMpa(mpa);
        assertTrue(tile.isProtected());
        tile.setMpa(null);
        assertFalse(tile.isProtected());

    }
}