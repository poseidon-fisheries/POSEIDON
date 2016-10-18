package uk.ac.ox.oxfish.utility.adaptation;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.adaptation.maximization.AdaptationAlgorithm;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.adaptation.probability.FixedProbability;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A general algorithm to perform exploration/imitaiton/exploitation decisions possibly on a specific variable
 * Created by carrknight on 8/6/15.
 */
public class ExploreImitateAdaptation<T> extends AbstractAdaptation<T> {


    /**
     * function to grab eligible friends
     */
    private Function<Pair<Fisher,MersenneTwisterFast>,Collection<Fisher>> friendsExtractor;



    /**
     * what the agent ought to do to adapt
     */
    private final AdaptationAlgorithm<T> algorithm;

    /**
     * how the agent should judge himself and others
     */
    private final ObjectiveFunction<Fisher>  objective;



    private final AdaptationProbability probability;




    /**
     * holds the starting point of a randomization
     */
    private Pair<T,Double> explorationStart;

    private ImitationStart<T> imitationStart;



    @Override
    public T concreteAdaptation(Fisher toAdapt, FishState state, MersenneTwisterFast random) {

        //check your fitness and where you are
        double fitness = objective.computeCurrentFitness(toAdapt);
        T current =  getSensor().scan(toAdapt);

        //if you explored in the previous step
        if(explorationStart != null)
        {
            assert imitationStart == null;


            Double previousFitness = explorationStart.getSecond();
            T previous = explorationStart.getFirst();
            T decision = this.algorithm.judgeRandomization(random, toAdapt,
                                                           previousFitness,
                                                           fitness,
                                                           previous,
                                                           current);

            this.probability.judgeExploration(previousFitness, fitness);

            explorationStart = null;


            if(decision!=null) {
                if(decision == previous )
                    fitness = previousFitness;
                current = decision;
            }
            assert current != null;
        }

        //if you imitated in the previous step
        else if(imitationStart != null)
        {
            assert explorationStart == null;
            double previousFitness = imitationStart.getPreviousFitness();
            T previous = imitationStart.getPreviousDecision();
            T decision = this.algorithm.judgeImitation(random, toAdapt,
                                                       imitationStart.getFriend(),
                                                       previousFitness,
                                                       fitness,
                                                       previous,
                                                       current);

            imitationStart = null;
            if(decision!=null) {
                if(decision == previous )
                    fitness = previousFitness;
                current = decision;

            }

        }

        //you have no previous decisions to judge or you had but decided not to act upon them
        //you are now ready to check whether to explore or exploit

        double explorationProbability = probability.getExplorationProbability();
        //explore?
        if(explorationProbability>0 && random.nextBoolean(explorationProbability)) {

            T future = algorithm.randomize(random, toAdapt, fitness,current);
            explorationStart = new Pair<>(current,fitness);
            return future;
        }

        assert  explorationStart==null;

        //imitate?
        double imitationProbability = probability.getImitationProbability();

        Collection<Fisher> friends = friendsExtractor.apply(new Pair<>(toAdapt, random));
        if(imitationProbability>0 && friends!=null &&
                !friends.isEmpty() && random.nextBoolean(imitationProbability))
        {


            Pair<T, Fisher> imitation = algorithm.imitate(random,
                                                          toAdapt, fitness, current,
                                                          friends, objective, getSensor());
            if(imitation.getSecond() != null)
                imitationStart = new ImitationStart<>(imitation.getSecond(),fitness,imitation.getFirst());

            return imitation.getFirst();

        }

        return null;
    }

    public ExploreImitateAdaptation(
            Predicate<Fisher> validator,
            AdaptationAlgorithm<T> decision,
            Actuator<Fisher,T> actuator,
            Sensor<Fisher,T> sensor,
            ObjectiveFunction<Fisher> objective, double explorationProbability,
            double imitationProbability) {

        this(validator,
             decision, actuator, sensor, objective, explorationProbability, imitationProbability,
             new Function<Pair<Fisher, MersenneTwisterFast>, Collection<Fisher>>() {
                 @Override
                 public Collection<Fisher> apply(
                         Pair<Fisher, MersenneTwisterFast> input) {
                     return input.getFirst().getDirectedFriends();
                 }
             }
        );

    }

    public ExploreImitateAdaptation(
            Predicate<Fisher> validator,
            AdaptationAlgorithm<T> decision,
            Actuator<Fisher,T> actuator,
            Sensor<Fisher,T> sensor,
            ObjectiveFunction<Fisher> objective,
            AdaptationProbability probability) {
        super(sensor, actuator, validator);
        this.friendsExtractor = new Function<Pair<Fisher, MersenneTwisterFast>, Collection<Fisher>>() {
            @Override
            public Collection<Fisher> apply(
                    Pair<Fisher, MersenneTwisterFast> input) {
                return input.getFirst().getDirectedFriends();
            }
        };
        this.algorithm = decision;
        this.objective = objective;
        this.probability = probability;
    }

    public ExploreImitateAdaptation(
            Predicate<Fisher> validator,
            AdaptationAlgorithm<T> decision,
            Actuator<Fisher,T> actuator, Sensor<Fisher,T> sensor,
            ObjectiveFunction<Fisher> objective,
            double explorationProbability,
            double imitationProbability,
            Function<Pair<Fisher, MersenneTwisterFast>, Collection<Fisher>> friendsExtractor) {
        super(sensor, actuator, validator);
        this.friendsExtractor = friendsExtractor;
        this.algorithm = decision;
        this.objective = objective;
        this.probability = new FixedProbability(explorationProbability,imitationProbability);
    }

    public void onStart(FishState state, Fisher toAdapt){
        algorithm.start(state, toAdapt,
                        getSensor().scan(toAdapt) );
        probability.start(state,toAdapt);
    }



    @Override
    public void turnOff(Fisher fisher) {

        probability.turnOff(fisher);
    }


    public Function<Pair<Fisher, MersenneTwisterFast>, Collection<Fisher>> getFriendsExtractor() {
        return friendsExtractor;
    }

    public void setFriendsExtractor(
            Function<Pair<Fisher, MersenneTwisterFast>, Collection<Fisher>> friendsExtractor) {
        this.friendsExtractor = friendsExtractor;
    }

    public AdaptationAlgorithm<T> getAlgorithm() {
        return algorithm;
    }


    public ObjectiveFunction<Fisher> getObjective() {
        return objective;
    }



    public Pair<T, Double> getExplorationStart() {
        return explorationStart;
    }

    public void setExplorationStart(Pair<T, Double> explorationStart) {
        this.explorationStart = explorationStart;
    }


    private class ImitationStart<K>
    {
        private final Fisher friend;
        private final double previousFitness;
        private final K previousDecision;

        public ImitationStart(Fisher friend, double previousFitness, K previousDecision) {
            this.friend = friend;
            this.previousFitness = previousFitness;
            this.previousDecision = previousDecision;
        }

        public Fisher getFriend() {
            return friend;
        }

        public double getPreviousFitness() {
            return previousFitness;
        }

        public K getPreviousDecision() {
            return previousDecision;
        }
    }
}
