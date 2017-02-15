package uk.ac.ox.oxfish.fisher.selfanalysis;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Adaptation;
import uk.ac.ox.oxfish.utility.adaptation.ExploreImitateAdaptation;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.adaptation.maximization.RandomStep;
import uk.ac.ox.oxfish.utility.adaptation.probability.FixedProbability;

import java.util.function.Predicate;

/**
 * Created by carrknight on 2/14/17.
 */
public class SocialNetworkAdaptation implements Adaptation<Integer> {


    private final ExploreImitateAdaptation<Integer> delegate;


    public SocialNetworkAdaptation(
            final int stepSize,
            final ObjectiveFunction<Fisher> objective,
            final double explorationProbability) {



        delegate =
                new ExploreImitateAdaptation<Integer>(
                        //don't change friends if you've been stuck a lot of time at home

                        (Predicate<Fisher>) fisher1 -> fisher1.getHoursAtPort() < 10*24,
                        new BeamHillClimbing<Integer>(new RandomStep<Integer>() {
                            @Override
                            public Integer randomStep(FishState state, MersenneTwisterFast random, Fisher fisher,
                                                      Integer current) {
                                return Math.min(
                                        Math.max( random.nextBoolean() ?
                                                          current + random.nextInt(stepSize) + 1 :
                                                          current - random.nextInt(stepSize) - 1,
                                                  0),
                                        state.getFishers().size() - 1);
                            }
                        })
                        ,
                        (Actuator<Fisher, Integer>) (subject, policy, model) -> {
                            int originalDirectedNeighbors = model.getSocialNetwork().getDirectedNeighbors(subject).size();
                            int target = Math.min(Math.max(policy, 0), model.getFishers().size() - 1);
                            int difference = target - model.getSocialNetwork().getBackingnetwork().getPredecessorCount(subject);
                            if (difference > 0) {
                                for (int i = 0; i < difference; i++)
                                    model.getSocialNetwork().addRandomConnection(subject,model.getFishers(),new MersenneTwisterFast());
                                ;
                            }
                            else if (difference < 0) {
                                for (int i = 0; i < -difference; i++)
                                {
                                    model.getSocialNetwork().removeRandomConnection(subject, model.getRandom());
                                }
                            }
                            Preconditions.checkArgument(
                                    model.getSocialNetwork().getBackingnetwork().getPredecessorCount(subject) == target);
                            assert  originalDirectedNeighbors == model.getSocialNetwork().getDirectedNeighbors(subject).size();
                        },
                        (Sensor<Fisher, Integer>) system -> system.getSocialNetwork().getBackingnetwork().getPredecessorCount(system),
                        objective,
                        new FixedProbability(explorationProbability, 0),
                        (Predicate<Integer>) integer -> true


                );
    }


    @Override
    public void start(FishState model, Fisher fisher) {
        delegate.start(model,fisher);
    }

    @Override
    public void turnOff(Fisher fisher) {
        delegate.turnOff(fisher);
    }

    /**
     * Ask yourself to adapt
     *
     * @param toAdapt who is doing the adaptation
     * @param state
     * @param random  the randomizer
     */
    @Override
    public void adapt(Fisher toAdapt, FishState state, MersenneTwisterFast random) {
        delegate.adapt(toAdapt, state, random);
    }
}
