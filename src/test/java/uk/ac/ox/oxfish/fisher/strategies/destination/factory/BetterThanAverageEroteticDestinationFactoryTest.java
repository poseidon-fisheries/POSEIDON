package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.factory.IndependentLogisticFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by carrknight on 5/2/16.
 */
public class BetterThanAverageEroteticDestinationFactoryTest
{


    @Test
    public void plainThresholdDoesNotClearMap() throws Exception {

        int steps = stepsItTookErotetic(2000, System.currentTimeMillis(), false);
        assertEquals(2000,steps);

    }

    @Test
    public void outdoingAverageWorks() throws Exception {

        int steps = stepsItTookErotetic(2000, System.currentTimeMillis(), true);
        assertTrue(2000>steps);

    }

    public static int stepsItTookErotetic(
            int maxSteps,
            final long seed,
            final boolean adaptive) {


        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(new IndependentLogisticFactory()); //skip migration which should make this faster.
        scenario.setFishers(300);
        if(adaptive)
            scenario.setDestinationStrategy(new BetterThanAverageEroteticDestinationFactory());
        else {
            ThresholdEroteticDestinationFactory plainThreshold = new ThresholdEroteticDestinationFactory();
            plainThreshold.setProfitThreshold(new FixedDoubleParameter(0d));
            scenario.setDestinationStrategy(plainThreshold);
        }

        FishState state = new FishState(seed, 1);
        state.setScenario(scenario);
        state.start();
        Species onlySpecies = state.getBiology().getSpecie(0);
        final double minimumBiomass = state.getTotalBiomass(
                onlySpecies) * .05; //how much does it take to eat 95% of all the fish?


        int steps;
        for (steps = 0; steps < maxSteps; steps++) {
            state.schedule.step(state);
            if (state.getTotalBiomass(onlySpecies) <= minimumBiomass)
                break;
        }
        //   System.out.println(steps + " -- " + state.getTotalBiomass(onlySpecies));
        return steps;


    }

}