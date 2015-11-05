package uk.ac.ox.oxfish.demoes;


import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomCatchabilityTrawlFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertTrue;

public class ExpensiveGasMakesYouFishAtHome
{




    //raise the price of gas and you will see boats move back


    @Test
    public void gasMakesYouThinkTwiceAboutGoingFarAway() throws Exception {


        PrototypeScenario scenario = new PrototypeScenario();

        scenario.setFishers(100);
        scenario.setHoldSize(new FixedDoubleParameter(500));
        SimpleMapInitializerFactory simpleMap = new SimpleMapInitializerFactory();
        simpleMap.setCellSizeInKilometers(new FixedDoubleParameter(2d));
        scenario.setMapInitializer(simpleMap);
        scenario.setBiologyInitializer(new FromLeftToRightFactory());

        RandomCatchabilityTrawlFactory gear = new RandomCatchabilityTrawlFactory();
        gear.setTrawlSpeed(new FixedDoubleParameter(0));
        scenario.setGear(gear);


        FishState state = new FishState(System.currentTimeMillis(),1);
        state.setScenario(scenario);
        state.start();


        //let one year pass
        for(int i=0; i<365;i++)
            state.schedule.step(state);

        //compute average distance from port
        double averageX = state.getFishers().stream().mapToDouble(
                value -> value.getLastFinishedTrip().getTilesFished().iterator().next().getGridX()).sum();
        averageX /=100;



        //increase the price of gas!
        state.getPorts().iterator().next().setGasPricePerLiter(5);
        //run for another year
        for(int i=0; i<365;i++)
            state.schedule.step(state);

        double newAverage = state.getFishers().stream().mapToDouble(
                value -> value.getLastFinishedTrip().getTilesFished().iterator().next().getGridX()).sum();
        newAverage /=100;

        System.out.println(averageX + " --- " + newAverage);

        assertTrue(1.5 * averageX < newAverage); //before the price rise the distance from port was at least 50% more!

    }
}
