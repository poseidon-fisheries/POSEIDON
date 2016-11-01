package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.destination.PerTripIterativeDestinationStrategy;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.mpa.StartingMPA;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.*;


public class PerTripImitativeDestinationFactoryTest {


    /**
     * 5 fishers, how often do they go on MPAs?
     */

    @Test
    public void fiveguysNotAvoid() throws Exception {

        int protectedTargets = protectedTargetsHit(false);

        assertTrue(protectedTargets > 0);
        System.out.println(protectedTargets);


    }


    @Test
    public void fiveguysAvoid() throws Exception {

        int protectedTargets = protectedTargetsHit(true);

        assertTrue(protectedTargets == 0);
        System.out.println(protectedTargets);


    }

    public int protectedTargetsHit(final boolean avoidMPAs) {
        PrototypeScenario scenario = new PrototypeScenario();
        PerTripImitativeDestinationFactory destinationStrategy = new PerTripImitativeDestinationFactory();
        destinationStrategy.setAutomaticallyIgnoreMPAs(avoidMPAs);
        scenario.setDestinationStrategy(destinationStrategy);
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setWidth(new FixedDoubleParameter(5));
        mapInitializer.setHeight(new FixedDoubleParameter(5));
        scenario.setMapInitializer(mapInitializer);
        scenario.setFishers(5);

        scenario.getStartingMPAs().add(new StartingMPA(0, 0, 2, 2));

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();

        //burn in
        for(int i=0; i<20; i++)
            state.schedule.step(state);
        int protectedTargets = 0;
        for(int i=0; i<100; i++)
        {
            state.schedule.step(state);
            for(Fisher fisher : state.getFishers())
                if(((PerTripIterativeDestinationStrategy) fisher.getDestinationStrategy()).getDelegate().getFavoriteSpot().isProtected())
                    protectedTargets++;
        }
        return protectedTargets;
    }
}