package uk.ac.ox.oxfish.experiments;

import com.esotericsoftware.minlog.Log;
import uk.ac.ox.oxfish.biology.initializer.factory.SplitInitializerFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.OneSpecieGearFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * World is split in blue and red, people can only one species. Will they reconnect in a way that avoids friends
 * with the wrong gear?
 * Created by carrknight on 12/4/15.
 */
public class ChangingNetworks {


    public static void main(String[] args) throws IOException {
        FishState state = new FishState(System.currentTimeMillis());
        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);

        PerTripImitativeDestinationFactory imitation = new PerTripImitativeDestinationFactory();
        imitation.setDynamicFriendshipNetwork(true);
        imitation.setIgnoreEdgeDirection(false);
        scenario.setDestinationStrategy(imitation);
        EquidegreeBuilder networkBuilder = new EquidegreeBuilder();
        networkBuilder.setDegree(5);
        scenario.setNetworkBuilder(networkBuilder);


        OneSpecieGearFactory option1 = new OneSpecieGearFactory();
        option1.setSpecieTargetIndex(0);
        OneSpecieGearFactory option2 = new OneSpecieGearFactory();
        option2.setSpecieTargetIndex(1);

        scenario.setGear(new AlgorithmFactory<Gear>() {
            int counter;
            @Override
            public Gear apply(FishState fishState) {
                counter++;
                Log.info("counter: " + counter);
                if(counter<=50)
                    return option1.apply(fishState);
                else
                    return option2.apply(fishState);
            }
        });


        scenario.setBiologyInitializer(new SplitInitializerFactory());
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        scenario.setMapInitializer(mapInitializer);

        state.start();

        Files.write(Paths.get("runs","networks","before.txt"),state.getSocialNetwork().toMatrixFile().getBytes());
        System.out.println();

        //run for 20 years
        while(state.getYear()<20)
            state.schedule.step(state);

        Files.write(Paths.get("runs","networks","after.txt"),state.getSocialNetwork().toMatrixFile().getBytes());



    }
}
