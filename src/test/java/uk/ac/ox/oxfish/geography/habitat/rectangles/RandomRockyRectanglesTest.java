package uk.ac.ox.oxfish.geography.habitat.rectangles;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 *
 * Created by carrknight on 11/18/15.
 */
public class RandomRockyRectanglesTest {


    @Test
    public void randomRocky() throws Exception {


        FishState state = MovingTest.generateSimple4x4Map();
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());

        RockyRectangleMaker maker = new RandomRockyRectangles(
                new FixedDoubleParameter(2),
                new FixedDoubleParameter(2),
                3);

        RockyRectangle[] rockyRectangles = maker.buildRectangles(state.getRandom(), state.getMap());
        assertEquals(rockyRectangles.length,3);
        for(RockyRectangle rectangle : rockyRectangles) {
            assertEquals(rectangle.getHeight(), 2);
            assertEquals(rectangle.getWidth(), 2);
        }

    }
}