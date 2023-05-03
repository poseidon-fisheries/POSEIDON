/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.experiments;

import com.esotericsoftware.minlog.Log;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

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
        imitation.setDropInUtilityNeededForUnfriend(new FixedDoubleParameter(0.027688));
        imitation.setIgnoreEdgeDirection(false);
        imitation.setAlwaysCopyBest(false);
        scenario.setDestinationStrategy(imitation);
        EquidegreeBuilder networkBuilder = new EquidegreeBuilder();
        networkBuilder.setDegree(new FixedDoubleParameter(1));
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
                                                              System.out.println(fisher.getID() +"--->" + friend.getID());
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

        //lspiRun for 20 years
        while(state.getYear()<20)
            state.schedule.step(state);

        Files.write(Paths.get("runs","networks","after.txt"),state.getSocialNetwork().toMatrixFile().getBytes());
        System.out.println(state.getYearlyDataSet().getColumn("Same Gear Friends").copy());



    }
}
