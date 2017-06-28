package uk.ac.ox.oxfish.model.regs.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 6/28/17.
 */
public class DepthMPAFactoryTest {


    @Test
    public void coversAll() throws Exception {

        //4x4 generated map is all at depth of 100

        FishState state = MovingTest.generateSimple4x4Map();
        DepthMPAFactory factory = new DepthMPAFactory();
        factory.setMaxDepth(new FixedDoubleParameter(200));
        factory.setMinDepth(new FixedDoubleParameter(0));
        factory.apply(state);
        for(SeaTile tile : state.getMap().getAllSeaTilesExcludingLandAsList())
            assertTrue(tile.isProtected());


    }

    @Test
    public void coversNone() throws Exception {

        //4x4 generated map is all at depth of 100

        FishState state = MovingTest.generateSimple4x4Map();
        DepthMPAFactory factory = new DepthMPAFactory();
        factory.setMaxDepth(new FixedDoubleParameter(200));
        factory.setMinDepth(new FixedDoubleParameter(101));
        factory.apply(state);
        for(SeaTile tile : state.getMap().getAllSeaTilesExcludingLandAsList())
            assertTrue(!tile.isProtected());


    }

    @Test
    public void coversNone2() throws Exception {

        //4x4 generated map is all at depth of 100

        FishState state = MovingTest.generateSimple4x4Map();
        DepthMPAFactory factory = new DepthMPAFactory();
        factory.setMaxDepth(new FixedDoubleParameter(99));
        factory.setMinDepth(new FixedDoubleParameter(0));
        factory.apply(state);
        for(SeaTile tile : state.getMap().getAllSeaTilesExcludingLandAsList())
            assertTrue(!tile.isProtected());


    }
}