package uk.ac.ox.oxfish.model.ga;

import ec.util.MersenneTwisterFast;
import org.jenetics.DoubleChromosome;
import org.jenetics.DoubleGene;
import org.jenetics.Genotype;
import org.junit.Test;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Gear;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedProbabilityDepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;

import java.util.LinkedList;


import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * a spike on how the fisher evolution should be used
 * Created by carrknight on 5/4/15.
 */
public class FisherEvolutionTest {


    @Test
    public void mockEvolution() throws Exception {

        //it will pretend to evolve, the higher the probability of departing the more fit the agent is

        FishState model = mock(FishState.class);
        MersenneTwisterFast random = new MersenneTwisterFast();
        when(model.seed()).thenReturn(random.nextLong());

        LinkedList<Fisher> fishers = new LinkedList<>();
        when(model.getFishers()).thenReturn(fishers);
        for(int i=0; i<100; i++)
            fishers.add(new Fisher(i, mock(Port.class),
                                     random,
                                     new Anarchy(),
                                     new FixedProbabilityDepartingStrategy(random.nextDouble()),
                                     mock(DestinationStrategy.class),
                                     mock(FishingStrategy.class),
                                     mock(Boat.class), mock(Hold.class),
                                     mock(Gear.class)));

        DoubleGene gene = DoubleGene.of(0.5,0,1);

        //now I have a population
        FisherEvolution<DoubleGene> evolution =
                //notice that we have 2 chromosomes. The first holds actually information (the departing strategy)
                //the second is just a random number so that we have a unique map fisher<--->chromosome
                new FisherEvolution<>(() -> Genotype.of(new DoubleChromosome(0d, 1d),(new DoubleChromosome(0d, 1d))),
                                      fisher -> {
                                          double probabilityToLeavePort = ((FixedProbabilityDepartingStrategy) fisher.getDepartingStrategy()).getProbabilityToLeavePort();
                                          return Genotype.of(DoubleChromosome.of(
                                                  DoubleGene.of(probabilityToLeavePort, 0d,
                                                                1d)),(new DoubleChromosome(0d, 1d)));

                                      },
                                      fisherGenotypePair -> fisherGenotypePair.getFirst().setDepartingStrategy(
                                              new FixedProbabilityDepartingStrategy(fisherGenotypePair.getSecond().getGene().doubleValue())));


        evolution.setFitness(
                fisher -> ((FixedProbabilityDepartingStrategy) fisher.getDepartingStrategy()).getProbabilityToLeavePort());
        evolution.start(model);

        double preAverage = 0;
        for(Fisher fisher : fishers)
            preAverage += ((FixedProbabilityDepartingStrategy) fisher.getDepartingStrategy()).getProbabilityToLeavePort();
        preAverage = (preAverage / fishers.size());

        for(int i=0;i<100;i++)
            evolution.step(model);
        double afterAverage = 0;
        for(Fisher fisher : fishers)
            afterAverage += ((FixedProbabilityDepartingStrategy) fisher.getDepartingStrategy()).getProbabilityToLeavePort();
        afterAverage = (afterAverage/fishers.size());

        System.out.println("pre:" + preAverage + ", after: " + afterAverage);
        assertTrue(afterAverage > .9); //the top is 1 but there is mutation
        assertTrue(afterAverage > preAverage);

    }
}