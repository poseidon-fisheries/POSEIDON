package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripIterativeDestinationFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.stream.IntStream;

/**
 * Here I store what I did in order to produce documents and such
 * Created by carrknight on 7/2/15.
 */
public class FriendsHillClimbing
{

    public static void testThatFriendsAreGood()
    {

        //run the prototype (2nd July 2015 edition)
        //make the agents act by hillclimber
        //and compare it with and without friends. Depending on the exploration parameter, with friends is better.


        for(int friends=0;friends<=20;friends++)
        {
            int[] stepsItTook = new int[100];
            for(int run=0; run<5; run++)
            {

                PrototypeScenario scenario = new PrototypeScenario();
                if(friends ==0) {
                    scenario.setNetworkBuilder(new EmptyNetworkBuilder());
                    scenario.setDestinationStrategy(new PerTripIterativeDestinationFactory());
                }
                else
                {
                    final EquidegreeBuilder networkBuilder = new EquidegreeBuilder();
                    networkBuilder.setDegree(friends);
                    scenario.setNetworkBuilder(networkBuilder);
                    final PerTripImitativeDestinationFactory destinationStrategy = new PerTripImitativeDestinationFactory();
                    destinationStrategy.setExplorationProbability(new FixedDoubleParameter(.20));
                    destinationStrategy.setIgnoreEdgeDirection(false);
                    scenario.setDestinationStrategy(destinationStrategy);
                }
                FishState state = new FishState(run,1);
                state.setScenario(scenario);
                state.start();
                Specie onlySpecie = state.getBiology().getSpecie(0);
                final double minimumBiomass= state.getTotalBiomass(onlySpecie)*.05; //how much does it take to eat 95% of all the fish?


                int steps = 0;
                for(steps = 0; steps<3500; steps++)
                {
                    state.schedule.step(state);
                    if(state.getTotalBiomass(onlySpecie)<=minimumBiomass)
                        break;
                }
             //   System.out.println(steps + " -- " + state.getTotalBiomass(onlySpecie));
                stepsItTook[run] =steps;


            }
            System.out.println(friends + "," + IntStream.of(stepsItTook).sum()/5);

        }


    }


    public static void main(String[] args)
    {
        testThatFriendsAreGood();
    }

}
