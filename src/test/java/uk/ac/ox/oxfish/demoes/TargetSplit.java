package uk.ac.ox.oxfish.demoes;

import com.esotericsoftware.minlog.Log;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.SplitInitializerFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.factory.TargetSpeciesObjectiveFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * If you make fishers only care about red, they'll fish mostly red
 * Created by carrknight on 3/24/16.
 */
public class TargetSplit {

    @Test
    public void targetObjective() throws Exception {
        Log.info("fishers are given an objective functions that makes them consider only red earnings; they should fish only red then");

        final FishState state = new FishState(System.currentTimeMillis());
        //world split in half


        SplitInitializerFactory biologyFactory = new SplitInitializerFactory();


        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(biologyFactory);

        PerTripImitativeDestinationFactory destinationFactory = new PerTripImitativeDestinationFactory();
        TargetSpeciesObjectiveFactory objectiveFunction = new TargetSpeciesObjectiveFactory();
        objectiveFunction.setOpportunityCosts(true);
        objectiveFunction.setSpeciesIndex(0);
        destinationFactory.setObjectiveFunction(
                objectiveFunction
        );
        scenario.setDestinationStrategy(destinationFactory);

        SimpleMapInitializerFactory simpleMap = new SimpleMapInitializerFactory();
        simpleMap.setCoastalRoughness(new FixedDoubleParameter(0d));
        scenario.setMapInitializer(simpleMap);
        scenario.forcePortPosition(new int[]{40, 25});

        scenario.setUsePredictors(false);


        long towsNorth = 0;
        long towsSouth = 0;

        state.start();

        while (state.getYear() < 3) {
            state.schedule.step(state);
            for (int x = 0; x < 50; x++) {
                for (int y = 0; y <= 25; y++) {
                    towsNorth += state.getMap().getDailyTrawlsMap().get(x, y);
                }
                for (int y = 26; y < 50; y++) {
                    towsSouth += state.getMap().getDailyTrawlsMap().get(x, y);
                }
            }
        }

        Log.info("North vs South : " + towsNorth / ((double) towsNorth + towsSouth) + " I expect it to be at least 70%");
        Assert.assertTrue(towsNorth / ((double) towsNorth + towsSouth) > .7);

    }
}
