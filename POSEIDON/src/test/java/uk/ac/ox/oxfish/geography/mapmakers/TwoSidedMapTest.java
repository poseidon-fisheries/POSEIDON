package uk.ac.ox.oxfish.geography.mapmakers;

import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.*;

public class TwoSidedMapTest {


    @Test
    public void twoSidedMap() {


        PrototypeScenario scenario = new PrototypeScenario();
        final TwoSidedMapFactory mapInitializer = new TwoSidedMapFactory();
        mapInitializer.setCellSizeInKilometers(new FixedDoubleParameter(100));
        mapInitializer.setHeight(new FixedDoubleParameter(3));
        mapInitializer.setWidth(new FixedDoubleParameter(10));
        scenario.setMapInitializer(
                mapInitializer
        );
        scenario.setFishers(1);
        FishState state = new FishState(1);
        state.setScenario(scenario);

        state.start();

        assertTrue(state.getMap().getSeaTile(0,0).isLand());
        assertTrue(state.getMap().getSeaTile(9,0).isLand());
        assertTrue(state.getMap().getSeaTile(9,2).isLand());
        assertTrue(state.getMap().getSeaTile(5,0).isWater());
        assertTrue(state.getMap().getSeaTile(1,1).isWater());
        assertTrue(state.getMap().getSeaTile(8,2).isWater());

        assertEquals(900,
                state.getMap().distance(0,0,9,0),
                .01);


    }
}