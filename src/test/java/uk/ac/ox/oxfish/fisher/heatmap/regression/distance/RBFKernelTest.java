package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 8/15/16.
 */
public class RBFKernelTest {

    @Test
    public void fartherLessKernel() throws Exception {

        RegressionDistance distance = new RegressionDistance() {
            @Override
            public double distance(
                    Fisher fisher, SeaTile tile, double currentTimeInHours, GeographicalObservation observation) {
                return tile.getGridX()-observation.getX();
            }
        };

        SeaTile center = new SeaTile(0,0,0d,new TileHabitat(0));
        SeaTile oneone = new SeaTile(1,1,0d,new TileHabitat(0));
        SeaTile twotwo = new SeaTile(2,2,0d,new TileHabitat(0));
        SeaTile minusone = new SeaTile(-1,-1,0d,new TileHabitat(0));

        RBFKernel kernel = new RBFKernel(distance, 5);
        //kernel is more similar for 0,0 to 1,1 than 0,0 to 2,2
        assertTrue(
                kernel.distance(mock(Fisher.class),center,0d,new GeographicalObservation(oneone,0d,null))
                        >
                        kernel.distance(mock(Fisher.class),center,0d,new GeographicalObservation(twotwo,0d,null))
        );
        //simmetric
        assertEquals(
                kernel.distance(mock(Fisher.class),center,0d,new GeographicalObservation(oneone,0d,null)),
                kernel.distance(mock(Fisher.class),center,0d,new GeographicalObservation(minusone,0d,null)),
                0.001
        );

        //max is 1
        assertEquals(
                kernel.distance(mock(Fisher.class),center,0d,new GeographicalObservation(center,0d,null)),
                1d,
                0.001
        );

        //numbers come out correctly
        assertEquals(
                kernel.distance(mock(Fisher.class),center,0d,new GeographicalObservation(twotwo,0d,null)),
                Math.exp(-4d/5),
                0.001
        );
    }
}