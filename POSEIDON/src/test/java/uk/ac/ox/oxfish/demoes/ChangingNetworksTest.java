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

package uk.ac.ox.oxfish.demoes;

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
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class ChangingNetworksTest {

    //@Test
    //this test stopped being true
    //we used to have people switching friends because they would copy them but they had the wrong gear
    //so that the network would converge to people only befriending others with same gear
    //however we made imitation smarter which in turn made this segregation effect not happening
    //because now I just ahead of time stop
    public void networksRemainSegregated() throws Exception {
        final FishState state = new FishState(System.currentTimeMillis());
        final PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);

        final PerTripImitativeDestinationFactory imitation = new PerTripImitativeDestinationFactory();
        imitation.setDropInUtilityNeededForUnfriend(new FixedDoubleParameter(0.027688));
        imitation.setIgnoreEdgeDirection(false);
        imitation.setAlwaysCopyBest(false);
        scenario.setDestinationStrategy(imitation);
        final EquidegreeBuilder networkBuilder = new EquidegreeBuilder();
        networkBuilder.setDegree(new FixedDoubleParameter(1));
        scenario.setNetworkBuilder(networkBuilder);


        state.getYearlyDataSet().registerGatherer(
            "Same Gear Friends",
            fishState -> {
                double sameGearConnections = 0;
                double connections = 0;
                for (final Fisher fisher : fishState.getFishers()) {
                    final Collection<Fisher> friends = fisher.getDirectedFriends();
                    connections += friends.size();
                    for (final Fisher friend : friends) {
                        if (friend.getID() < 50 && fisher.getID() < 50) {
                            sameGearConnections++;
                        } else if (friend.getID() >= 50 && fisher.getID() >= 50) {
                            sameGearConnections++;
                        }
                    }

                }
                return sameGearConnections / connections;
            },
            Double.NaN
        );


        final OneSpecieGearFactory option1 = new OneSpecieGearFactory();
        option1.setSpecieTargetIndex(0);
        final OneSpecieGearFactory option2 = new OneSpecieGearFactory();
        option2.setSpecieTargetIndex(1);

        scenario.setGear(new AlgorithmFactory<Gear>() {
            int counter;

            @Override
            public Gear apply(final FishState fishState) {
                counter++;
                if (counter <= 50)
                    return option1.apply(fishState);
                else
                    return option2.apply(fishState);
            }
        });


        scenario.setBiologyInitializer(new SplitInitializerFactory());
        final SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        scenario.setMapInitializer(mapInitializer);

        state.start();


        //lspiRun for 5 years
        while (state.getYear() < 5)
            state.schedule.step(state);
        assertTrue(state.getLatestYearlyObservation("Same Gear Friends") > .9);

    }
}
