package uk.ac.ox.oxfish.geography.mapmakers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class SimpleMapInitializerTest {


    @Test
    public void depthIsRight() {

        final PrototypeScenario scenario = new PrototypeScenario();
        final SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();

        mapInitializer.setDepthSmoothing(new FixedDoubleParameter(0));
        mapInitializer.setMaxLandWidth(new FixedDoubleParameter(1));
        mapInitializer.setWidth(new FixedDoubleParameter(5));
        mapInitializer.setHeight(new FixedDoubleParameter(5));
        mapInitializer.setMaxInitialDepth(new FixedDoubleParameter(1000));
        mapInitializer.setMinInitialDepth(new FixedDoubleParameter(100));
        scenario.setMapInitializer(mapInitializer);
        scenario.setFishers(0);
        final FishState state = new FishState();
        state.setScenario(scenario);
        state.start();

        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++) {
                Assertions.assertTrue(state.getMap().getSeaTile(i, j).getAltitude() <= -100);
                Assertions.assertTrue(state.getMap().getSeaTile(i, j).getAltitude() >= -1000);
            }
    }
}