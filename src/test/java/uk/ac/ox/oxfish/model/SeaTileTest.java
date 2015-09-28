package uk.ac.ox.oxfish.model;

import org.junit.Test;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;


public class SeaTileTest {


    @Test
    public void recognizesMPA() {

        MasonGeometry mpa = mock(MasonGeometry.class);
        SeaTile tile = new SeaTile(0,0,0, new TileHabitat(0d));
        tile.assignMpa(mpa);
        assertTrue(tile.isProtected());
        tile.assignMpa(null);
        assertFalse(tile.isProtected());

    }
}