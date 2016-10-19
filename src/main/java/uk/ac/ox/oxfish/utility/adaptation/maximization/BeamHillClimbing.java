package uk.ac.ox.oxfish.utility.adaptation.maximization;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * An hill=climber that tries a new step on "randomize", copy a friend in "imitate" and stays put in "exploit".
 *
 * It is abstract as it expects a method to randomize a new step
 * Created by carrknight on 8/6/15.
 */
public class BeamHillClimbing<T> implements AdaptationAlgorithm<T>
{



    public T randomStep(FishState state,MersenneTwisterFast random, Fisher fisher, T current){
        return randomStep.randomStep(state,random,fisher,current);

    };

    private FishState model;

    /**
     * the default value of copyAlwaysBest
     */
    public final static boolean DEFAULT_ALWAYS_COPY_BEST = true;

    public final static boolean DEFAULT_BACKTRACKS_ON_BAD_EXPLORATION = true;

    /**
     * the default state of the unfriendPredicate field
     */
    public final static Predicate<Pair<Double,Double>> DEFAULT_DYNAMIC_NETWORK = new Predicate<Pair<Double, Double>>() {
        @Override
        public boolean test(Pair<Double, Double> doubleDoublePair) {
            return false;
        }
    };

    /**
     * if true imitation occurs by looking at your friend who is performing better, otherwise it
     * works by looking at a random friend. <br>
     *     In both cases we ignore friends who perform worse or equal to us
     */
    private final boolean copyAlwaysBest;

    /**
     * A function that judges whether to change a friend after imitating given the pair (previous fitness,newfitness).
     * When the function returns true, we will replace whoever we imitated with somebody at random
     */
    private final Predicate<Pair<Double,Double>> unfriendPredicate;


    /**
     * what is the result of an exploration step
     */
    private final RandomStep<T> randomStep;

    /**
     * if an exploration goes badly, does it go back to the previous spot ("the best" remembered)
     */
    private final boolean backtracksOnBadExploration;

    public BeamHillClimbing(
            boolean copyAlwaysBest,
            boolean backtracksOnBadExploration,
            Predicate<Pair<Double, Double>> unfriendPredicate,
            RandomStep<T> randomStep) {
        this.copyAlwaysBest = copyAlwaysBest;
        this.unfriendPredicate = unfriendPredicate;
        this.randomStep = randomStep;
        this.backtracksOnBadExploration = backtracksOnBadExploration;
    }

    public BeamHillClimbing(RandomStep<T> randomStep) {
        this(DEFAULT_ALWAYS_COPY_BEST,
             true, DEFAULT_DYNAMIC_NETWORK,
             randomStep);
    }

    @Override
    public void start(FishState model, Fisher agent, T initial) {
        this.model = model;
    }

    /**
     * new random step
     */
    @Override
    public T randomize(MersenneTwisterFast random, Fisher agent, double currentFitness, T current)
    {
        return randomStep(this.model,random,agent,current);
    }


    /**
     * copy friend. No problem
     */
    @Override
    public Pair<T, Fisher> imitate(
            MersenneTwisterFast random, Fisher agent, double fitness, T current, Collection<Fisher> friends,
            ObjectiveFunction<Fisher> objectiveFunction, Sensor<Fisher,T> sensor) {
        if(copyAlwaysBest)
            return FishStateUtilities.imitateBestFriend(random, fitness,
                                                            current, friends,
                                                            objectiveFunction, sensor);
        else
            return FishStateUtilities.imitateFriendAtRandom(random, fitness,
                                                            current, friends,
                                                            objectiveFunction, sensor);
    }

    @Override
    public T judgeRandomization(
            MersenneTwisterFast random, Fisher agent, double previousFitness, double currentFitness, T previous,
            T current) {
        if(backtracksOnBadExploration && previousFitness > currentFitness)
            return previous;
        else
            return current;
    }

    //stay still!
    @Override
    public T exploit(MersenneTwisterFast random, Fisher agent, double currentFitness, T current) {
        return current;
    }

    /**
     * returns null
     */
    @Override
    public T judgeImitation(
            MersenneTwisterFast random, Fisher agent, Fisher friendImitated, double fitnessBeforeImitating,
            double fitnessAfterImitating, T previous,
            T current) {
        if(unfriendPredicate.test(new Pair<>(fitnessBeforeImitating, fitnessAfterImitating)) )
            agent.replaceFriend(friendImitated,true);
        if(fitnessBeforeImitating > fitnessAfterImitating)
            return previous;
        else
            return current;
    }
}
