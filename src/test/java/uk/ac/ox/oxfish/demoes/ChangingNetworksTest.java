package uk.ac.ox.oxfish.demoes;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.SplitInitializerFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.OneSpecieGearFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Collection;

import static org.junit.Assert.assertTrue;


public class ChangingNetworksTest {

    @Test
    public void networksRemainSegregated() throws Exception {
        FishState state = new FishState(System.currentTimeMillis());
        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);

        PerTripImitativeDestinationFactory imitation = new PerTripImitativeDestinationFactory();
        imitation.setDropInUtilityNeededForUnfriend(new FixedDoubleParameter(0.027688));
        imitation.setIgnoreEdgeDirection(false);
        imitation.setAlwaysCopyBest(false);
        scenario.setDestinationStrategy(imitation);
        EquidegreeBuilder networkBuilder = new EquidegreeBuilder();
        networkBuilder.setDegree(1);
        scenario.setNetworkBuilder(networkBuilder);


        state.getYearlyDataSet().registerGatherer("Same Gear Friends",
                                                  fishState -> {
                                                      double sameGearConnections = 0;
                                                      double connections = 0;
                                                      for(Fisher fisher : fishState.getFishers())
                                                      {
                                                          Collection<Fisher> friends = fisher.getDirectedFriends();
                                                          connections += friends.size();
                                                          for(Fisher friend : friends)
                                                          {
                                                              if(friend.getID() < 50 && fisher.getID() < 50) {
                                                                  sameGearConnections++;
                                                              }
                                                              else  if (friend.getID() >= 50 && fisher.getID() >= 50) {
                                                                  sameGearConnections++;
                                                              }
                                                          }

                                                      }
                                                      return sameGearConnections/connections;
                                                  },
                                                  Double.NaN);


        OneSpecieGearFactory option1 = new OneSpecieGearFactory();
        option1.setSpecieTargetIndex(0);
        OneSpecieGearFactory option2 = new OneSpecieGearFactory();
        option2.setSpecieTargetIndex(1);

        scenario.setGear(new AlgorithmFactory<Gear>() {
            int counter;
            @Override
            public Gear apply(FishState fishState) {
                counter++;
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


        //run for 5 years
        while(state.getYear()<5)
            state.schedule.step(state);
        assertTrue(state.getLatestYearlyObservation("Same Gear Friends")>.9);

    }
}