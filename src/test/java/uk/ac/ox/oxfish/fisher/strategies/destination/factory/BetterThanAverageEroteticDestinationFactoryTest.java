package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import com.esotericsoftware.minlog.Log;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.factory.IndependentLogisticFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertTrue;

/**
 * Created by carrknight on 5/2/16.
 */
public class BetterThanAverageEroteticDestinationFactoryTest
{


    @Test
    public void plainThresholdDoesNotClearMap() throws Exception {

        long seed = System.currentTimeMillis();
        int steps = stepsItTookErotetic(2000, seed, false);
        int steps2 = stepsItTookErotetic(2000, seed, true);
        assertTrue(steps>steps2);

    }


    public static int stepsItTookErotetic(
            int maxSteps,
            final long seed,
            final boolean adaptive) {


        Log.set(Log.LEVEL_INFO);
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
                onlySpecies) * .1; //how much does it take to eat 90% of all the fish?


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