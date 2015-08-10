package uk.ac.ox.oxfish.utility.maximization;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Collection;

/**
 * An hill=climber that tries a new step on "randomize", copy a friend in "imitate" and stays put in "exploit".
 *
 * It is abstract as it expects a method to randomize a new step
 * Created by carrknight on 8/6/15.
 */
public abstract class BeamHillClimbing<T> implements AdaptationAlgorithm<T>
{



    abstract public T randomStep(FishState state,MersenneTwisterFast random, Fisher fisher, T current);

    private FishState model;


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
    public T imitate(
            MersenneTwisterFast random, Fisher agent, double fitness, T current, Collection<Fisher> friends,
            ObjectiveFunction<Fisher> objectiveFunction, Sensor<T> sensor) {
        return FishStateUtilities.imitateBestFriend(random, fitness,
                                                        current, friends,
                                                        objectiveFunction, sensor);
    }

    @Override
    public T judgeRandomization(
            MersenneTwisterFast random, Fisher agent, double previousFitness, double currentFitness, T previous,
            T current) {
        if(previousFitness > currentFitness)
            return previous;
        else
            return current;
    }

    //stay still!
    @Override
    public T exploit(MersenneTwisterFast random, Fisher agent, double currentFitness, T current) {
        return current;
    }
}
