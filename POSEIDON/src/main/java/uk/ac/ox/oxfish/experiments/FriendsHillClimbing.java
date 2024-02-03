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

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripIterativeDestinationFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.FixedProbabilityFactory;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.stream.IntStream;

/**
 * Here I store what I did in order to produce documents and such
 * Created by carrknight on 7/2/15.
 */
public class FriendsHillClimbing {

    public static void main(String[] args) {
        testThatFriendsAreGood();
    }

    public static void testThatFriendsAreGood() {

        //lspiRun the prototype (2nd July 2015 edition)
        //make the agents act by hillclimber
        //and compare it with and without friends. Depending on the exploration parameter, with friends is better.


        for (int friends = 0; friends <= 20; friends++) {
            int[] stepsItTook = new int[100];
            for (int run = 0; run < 5; run++) {

                PrototypeScenario scenario = new PrototypeScenario();
                if (friends == 0) {
                    scenario.setNetworkBuilder(new EmptyNetworkBuilder());
                    scenario.setDestinationStrategy(new PerTripIterativeDestinationFactory());
                } else {
                    final EquidegreeBuilder networkBuilder = new EquidegreeBuilder();
                    networkBuilder.setDegree(new FixedDoubleParameter(friends));
                    scenario.setNetworkBuilder(networkBuilder);
                    final PerTripImitativeDestinationFactory destinationStrategy = new PerTripImitativeDestinationFactory();
                    destinationStrategy.setProbability(new FixedProbabilityFactory(.20, 1d));
                    destinationStrategy.setIgnoreEdgeDirection(false);
                    scenario.setDestinationStrategy(destinationStrategy);
                }
                FishState state = new FishState(run, 1);
                state.setScenario(scenario);
                state.start();
                Species onlySpecies = state.getBiology().getSpecie(0);
                final double minimumBiomass = state.getTotalBiomass(onlySpecies) * .05; //how much does it take to eat 95% of all the fish?


                int steps = 0;
                for (steps = 0; steps < 3500; steps++) {
                    state.schedule.step(state);
                    if (state.getTotalBiomass(onlySpecies) <= minimumBiomass)
                        break;
                }
                //   System.out.println(steps + " -- " + state.getTotalBiomass(onlySpecies));
                stepsItTook[run] = steps;


            }
            System.out.println(friends + "," + IntStream.of(stepsItTook).sum() / 5);

        }


    }

}
