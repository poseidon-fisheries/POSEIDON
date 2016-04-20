package uk.ac.ox.oxfish.utility.adaptation;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
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
public class Adaptation<T> implements FisherStartable {


    /**
     * each "step" the validator makes sure the fisher is ready to adapt; if it returns false the adaptation is aborted
     */
    private Predicate<Fisher> validator;


    /**
     * function to grab eligible friends
     */
    private Function<Pair<Fisher,MersenneTwisterFast>,Collection<Fisher>> friendsExtractor;



    /**
     * what the agent ought to do to adapt
     */
    private final AdaptationAlgorithm<T> algorithm;

    /**
     * a class that assigns a new T to the fisher
     */
    private Actuator<T> actuator;

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

    private final Sensor<T> sensor;





    /**
     * reference to the model. passed to the actuator. Grabbed at start()
     */
    private FishState model;


    public Adaptation(
            Predicate<Fisher> validator,
            AdaptationAlgorithm<T> decision,
            Actuator<T> actuator,
            Sensor<T> sensor,
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

    public Adaptation(
            Predicate<Fisher> validator,
            AdaptationAlgorithm<T> decision,
            Actuator<T> actuator,
            Sensor<T> sensor,
            ObjectiveFunction<Fisher> objective,
            AdaptationProbability probability) {
        this.validator = validator;
        this.friendsExtractor = new Function<Pair<Fisher, MersenneTwisterFast>, Collection<Fisher>>() {
            @Override
            public Collection<Fisher> apply(
                    Pair<Fisher, MersenneTwisterFast> input) {
                return input.getFirst().getDirectedFriends();
            }
        };
        this.algorithm = decision;
        this.actuator = actuator;
        this.objective = objective;
        this.probability = probability;
        this.sensor = sensor;
    }

    public Adaptation(
            Predicate<Fisher> validator,
            AdaptationAlgorithm<T> decision,
            Actuator<T> actuator, Sensor<T> sensor,
            ObjectiveFunction<Fisher> objective,
            double explorationProbability,
            double imitationProbability,
            Function<Pair<Fisher, MersenneTwisterFast>, Collection<Fisher>> friendsExtractor) {
        this.validator = validator;
        this.friendsExtractor = friendsExtractor;
        this.algorithm = decision;
        this.actuator = actuator;
        this.objective = objective;
        this.probability = new FixedProbability(explorationProbability,imitationProbability);
        this.sensor = sensor;
    }

    public void start(FishState state, Fisher toAdapt){
        this.model = state;
        algorithm.start(state, toAdapt,sensor.scan(toAdapt) );
        probability.start(state,toAdapt);
    }

    /**
     * the simple loop: check whether to randomize, imitate or exploit
     * @param toAdapt who is doing the adaptation
     * @param random the randomizer
     */
    public void adapt(Fisher toAdapt, MersenneTwisterFast random)
    {

        //are you ready?
        if(!validator.test(toAdapt))
            return;


        //check your fitness and where you are
        double fitness = objective.computeCurrentFitness(toAdapt);
        T current =  sensor.scan(toAdapt);

        //if you explored in the previous step
        if(explorationStart != null)
        {
            assert imitationStart == null;



            T decision = this.algorithm.judgeRandomization(random, toAdapt,
                                                           explorationStart.getSecond(),
                                                           fitness,
                                                           explorationStart.getFirst(),
                                                           current);

            this.probability.judgeExploration(explorationStart.getSecond(),fitness);

            explorationStart = null;


            if(decision != null)
            {

                act(toAdapt, decision);
                return;
            }
        }

        //if you imitated in the previous step
        else if(imitationStart != null)
        {
            assert explorationStart == null;
            T decision = this.algorithm.judgeImitation(random,toAdapt,
                                                       imitationStart.getFriend(),
                                                       imitationStart.getPreviousFitness(),
                                                       fitness,
                                                       imitationStart.getPreviousDecision(),
                                                       current);

            imitationStart = null;
            if(decision != null)
            {
                act(toAdapt, decision);
                return;
            }

        }

        //you have no previous decisions to judge or you had but decided not to act upon them
        //you are now ready to check whether to explore or exploit

        double explorationProbability = probability.getExplorationProbability();
        //explore?
        if(explorationProbability>0 && random.nextBoolean(explorationProbability)) {

            T future = algorithm.randomize(random, toAdapt, fitness,current);
            explorationStart = new Pair<>(current,fitness);
            act(toAdapt,future);
            return;
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
                                                        friends, objective, sensor);
            if(imitation.getSecond() != null)
                imitationStart = new ImitationStart<>(imitation.getSecond(),fitness,imitation.getFirst());

            act(toAdapt,
                imitation.getFirst());
            return;

        }


        //no imitation, no exploration
        //exploit:
        act(toAdapt,
            algorithm.exploit(random, toAdapt, fitness, current));


    }


    @Override
    public void turnOff() {
        probability.turnOff();
    }

    private void act(Fisher toAdapt,T newVariable)
    {
        if(newVariable != sensor.scan(toAdapt))
            actuator.apply(toAdapt,newVariable,model );
    }

    public Function<Pair<Fisher, MersenneTwisterFast>, Collection<Fisher>> getFriendsExtractor() {
        return friendsExtractor;
    }

    public void setFriendsExtractor(
            Function<Pair<Fisher, MersenneTwisterFast>, Collection<Fisher>> friendsExtractor) {
        this.friendsExtractor = friendsExtractor;
    }

    public Predicate<Fisher> getValidator() {
        return validator;
    }

    public AdaptationAlgorithm<T> getAlgorithm() {
        return algorithm;
    }

    public Actuator<T> getActuator() {
        return actuator;
    }

    public void setActuator(Actuator<T> actuator) {
        this.actuator = actuator;
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

    public Sensor<T> getSensor() {
        return sensor;
    }

    public void setValidator(Predicate<Fisher> validator) {
        this.validator = validator;
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
