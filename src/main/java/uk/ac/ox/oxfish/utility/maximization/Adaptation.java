package uk.ac.ox.oxfish.utility.maximization;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.utility.Pair;

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
    private Function<Pair<Fisher,MersenneTwisterFast>,Collection<Fisher>> friendsExtractor =
            input -> input.getFirst().getDirectedFriends();


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



    /**
     * probability of Exploring
     */
    private final double explorationProbability;

    /**
     * probability of imitatating (conditional on not exploring)
     */
    private final double imitationProbability;

    /**
     * holds the starting point of a randomization
     */
    private Pair<T,Double> explorationStart;

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
        this.validator = validator;
        this.algorithm = decision;
        this.actuator = actuator;
        this.objective = objective;
        this.explorationProbability = explorationProbability;
        this.imitationProbability = imitationProbability;
        this.sensor = sensor;
    }

    public Adaptation(
            Predicate<Fisher> validator,
            Function<Pair<Fisher, MersenneTwisterFast>, Collection<Fisher>> friendsExtractor,
            AdaptationAlgorithm<T> decision,
            Actuator<T> actuator,
            Sensor<T> sensor,
            ObjectiveFunction<Fisher> objective, double explorationProbability,
            double imitationProbability) {
        this.validator = validator;
        this.friendsExtractor = friendsExtractor;
        this.algorithm = decision;
        this.actuator = actuator;
        this.objective = objective;
        this.explorationProbability = explorationProbability;
        this.imitationProbability = imitationProbability;
        this.sensor = sensor;
    }

    public void start(FishState state, Fisher toAdapt){
        this.model = state;
        algorithm.start(state, toAdapt,sensor.scan(toAdapt) );
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
            T decision = this.algorithm.judgeRandomization(random, toAdapt,
                                                           explorationStart.getSecond(),
                                                           fitness,
                                                           explorationStart.getFirst(),
                                                           current);
            act(toAdapt, decision);

            //if(decision == explorationStart.getFirst())
              //  fitness=explorationStart.getSecond();
            //current = decision;


            explorationStart = null;


            //this return is somewhat fundamental
            return;


        }

        //you are ready

        //explore?
        if(explorationProbability>0 && random.nextBoolean(explorationProbability)) {

            T future = algorithm.randomize(random, toAdapt, fitness,current);
            explorationStart = new Pair<>(current,fitness);
            act(toAdapt,future);
            return;
        }

        assert  explorationStart==null;

        //imitate?
        Collection<Fisher> friends = friendsExtractor.apply(new Pair<>(toAdapt, random));
        if(imitationProbability>0 && !friends.isEmpty() && random.nextBoolean(imitationProbability))
        {

            act(toAdapt,
                algorithm.imitate(random,
                                  toAdapt, fitness, current,
                                  friends,objective,sensor));
            return;

        }


        //no imitation, no exploration
        //exploit:
        act(toAdapt,
            algorithm.exploit(random, toAdapt, fitness, current));


    }


    @Override
    public void turnOff() {
        //nothing really
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

    public double getExplorationProbability() {
        return explorationProbability;
    }

    public double getImitationProbability() {
        return imitationProbability;
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
}
