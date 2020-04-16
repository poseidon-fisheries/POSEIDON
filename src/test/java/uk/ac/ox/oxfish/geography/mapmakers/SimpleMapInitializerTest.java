package uk.ac.ox.oxfish.geography.mapmakers;

import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.*;

public class SimpleMapInitializerTest {


    @Test
    public void depthIsRight() {

        PrototypeScenario scenario = new PrototypeScenario();
        final SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();

        mapInitializer.setDepthSmoothing(new FixedDoubleParameter(0));
        mapInitializer.setMaxLandWidth(new FixedDoubleParameter(1));
        mapInitializer.setWidth(new FixedDoubleParameter(5));
        mapInitializer.setHeight(new FixedDoubleParameter(5));
        mapInitializer.setMaxInitialDepth(new FixedDoubleParameter(1000));
        mapInitializer.setMinInitialDepth(new FixedDoubleParameter(100));
        scenario.setMapInitializer(mapInitializer);
        scenario.setFishers(0);
        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();

        for(int i=0; i<4; i++)
            for(int j=0; j<4; j++) {
                assertTrue(
                        state.getMap().getSeaTile(i, j).getAltitude()<= -100
                        );
                assertTrue(
                        state.getMap().getSeaTile(i, j).getAltitude()>= -1000
                );
            }
    }
}