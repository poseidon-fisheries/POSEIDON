package uk.ac.ox.oxfish.model.scenario;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import static org.junit.Assert.assertEquals;

/**
 * Created by carrknight on 11/18/15.
 */
public class PrototypeScenarioTest {

    @Test
    public void startingMPAs() throws Exception {


        //make sure we can add MPAs from list
        String override = "Abstract:\n" +
                "  startingMPAs:\n" +
                "  - height: 6\n" +
                "    topLeftX: 0\n" +
                "    topLeftY: 0\n" +
                "    width: 5\n" +
                "  - height: 5\n" +
                "    topLeftX: 10\n" +
                "    topLeftY: 10\n" +
                "    width: 5\n";

        //read in the base scenario

        FishYAML yaml = new FishYAML();
        PrototypeScenario scenario = yaml.loadAs(override, PrototypeScenario.class);

        assertEquals(scenario.getStartingMPAs().size(),2);
        //the order can be flipped
        assertEquals(scenario.getStartingMPAs().get(0).getHeight(),5,1);
        assertEquals(scenario.getStartingMPAs().get(1).getHeight(),5,1);


    }


    @Test
    public void portPosition1() throws Exception {

        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setMapInitializer(new SimpleMapInitializerFactory(50, 50, 0, 1000000, 2));

        scenario.forcePortPosition(new int[]{40, 25});

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);

        state.start();
        Port port = state.getMap().getPorts().iterator().next();

        assertEquals(port.getLocation().getGridX(),40);
        assertEquals(port.getLocation().getGridY(),25);


    }


    @Test
    public void portPosition2() throws Exception {

        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setMapInitializer(new SimpleMapInitializerFactory(50, 50, 0, 1000000, 2));

        scenario.setPortPositionX(40);
        scenario.setPortPositionY(20);

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);

        state.start();
        Port port = state.getMap().getPorts().iterator().next();

        assertEquals(port.getLocation().getGridX(),40);
        assertEquals(port.getLocation().getGridY(),20);


    }


    @Test
    public void fixingTheSeedWorks() throws Exception
    {

        PrototypeScenario scenario = new PrototypeScenario();


        FishState state = new FishState(123l);
        state.setScenario(scenario);
        state.start();

        for(int i=0; i<400;i++)
            state.schedule.step(state);

        double landings = state.getDailyDataSet().getColumn("Species 0 Landings").stream().reduce(
                (aDouble, aDouble2) -> aDouble + aDouble2).get();


        long random = state.getRandom().nextLong();

        //now do it all over again, the result ought to be the same
        scenario = new PrototypeScenario();


        state = new FishState(123l);
        state.setScenario(scenario);
        state.start();

        for(int i=0; i<400;i++)
            state.schedule.step(state);

        double landings2 = state.getDailyDataSet().getColumn("Species 0 Landings").stream().reduce(
                (aDouble, aDouble2) -> aDouble + aDouble2).get();

        assertEquals(landings,landings2,.0001);
        System.out.println(random);
        assertEquals(random,state.getRandom().nextLong());

    }
}