package uk.ac.ox.oxfish.fisher.selfanalysis;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.FixedProportionGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.IgnoreWeatherStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.network.NetworkBuilders;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


public class GearImitationAnalysisTest
{


    @Test
    public void bestImitateWorstNotViceversa() throws Exception
    {
        //get two fishers
        Fisher best = mock(Fisher.class);
        Fisher worst = mock(Fisher.class);
        //they are friends with each other!
        when(best.getDirectedFriends()).thenReturn(Collections.singletonList(worst));
        when(worst.getDirectedFriends()).thenReturn(Collections.singletonList(best));
        //set their gear
        Gear bestGear = mock(Gear.class); when(bestGear.makeCopy()).thenReturn(bestGear); when(best.getGear()).thenReturn(bestGear);
        Gear worstGear = mock(Gear.class); when(worstGear.makeCopy()).thenReturn(worstGear); when(worst.getGear()).thenReturn(worstGear);


        //objective function is better for best than for worst
        @SuppressWarnings("unchecked")
        ObjectiveFunction<Fisher> fitness = mock(ObjectiveFunction.class);
        when(fitness.computeCurrentFitness(best)).thenReturn(1d);
        when(fitness.computeCurrentFitness(worst)).thenReturn(0d);

        //100% imitating, 0% randomizing
        GearImitationAnalysis bestAnalysis = new GearImitationAnalysis(0d,1d,new LinkedList<>(), fitness);
        GearImitationAnalysis worstAnalysis = new GearImitationAnalysis(0d,1d,new LinkedList<>(), fitness);

        //best should not switch gear. Worst should
        bestAnalysis.getAlgorithm().adapt(best,new MersenneTwisterFast());
        worstAnalysis.getAlgorithm().adapt(worst, new MersenneTwisterFast());


        verify(best,never()).setGear(any());
        verify(worst).setGear(bestGear);
    }


    /**
     * basically there is a strong monotonic ranking of gears. With 10% imitation is that good enough for the best
     * gear to emerge?
     */
    @Test
    public void bestGearWins()
    {
        List<Fisher> fishers = new LinkedList<>();
        List<GearImitationAnalysis> analyses = new LinkedList<>();
        ObjectiveFunction<Fisher> function = new ObjectiveFunction<Fisher>() {
            @Override
            public double computeCurrentFitness(Fisher observed) {
                return ((FixedProportionGear) observed.getGear()).getProportionFished();
            }

            @Override
            public double computePreviousFitness(Fisher observed) {
                throw new RuntimeException("not useeeed!");
            }
        };

        FishState state = mock(FishState.class);

        //create the agents
        for(int i=0;i<100;i++)
        {
            Gear gear = new FixedProportionGear(i);
            Fisher fisher = new Fisher(i,mock(Port.class),new MersenneTwisterFast(),
                                       mock(Regulation.class),mock(DepartingStrategy.class),
                                       mock(DestinationStrategy.class),mock(FishingStrategy.class),
                                       new IgnoreWeatherStrategy(), mock(Boat.class),mock(Hold.class),gear, state.getSpecies().size()
            );
            GearImitationAnalysis analysis = new GearImitationAnalysis(0d, 1d, new LinkedList<>(), function);

            fishers.add(fisher);
            analyses.add(analysis);
            analysis.start(state,fisher);
        }
        //create the social network
        SocialNetwork network = new SocialNetwork(NetworkBuilders.CONSTRUCTORS.get("Equal Out Degree").get());
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());
        when(state.getFishers()).thenReturn(fishers);
        when(state.getSocialNetwork()).thenReturn(network);
        network.populate(state);

        //start the fishers so that they learn about the social network
        for(Fisher fisher : fishers)
        {
            fisher.start(state);
        }



        //pre average ought to be 49.5 (from 0 to 99)
        double average = 0;
        for(Fisher fisher : fishers)
        {
            average += ((FixedProportionGear) fisher.getGear()).getProportionFished();
        }
        average/=100;
        System.out.println(average);
        assertEquals(average,49.5,.1);



        //okay now step it for 100 times, see if the technology has improved:
        for(int steps =0; steps<100;steps++)
        {
            for(GearImitationAnalysis analysis : analyses)
                analysis.getAlgorithm().adapt(analysis.getFisher(),
                                              state.getRandom());
        }

        //the average ought to be high, at least 90
        average = 0;
        for(Fisher fisher : fishers)
        {
            average += ((FixedProportionGear) fisher.getGear()).getProportionFished();
        }
        average/=100;
        System.out.println(average);
        assertTrue(average > 90);
    }
}