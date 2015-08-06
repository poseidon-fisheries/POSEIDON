package uk.ac.ox.oxfish.fisher.selfanalysis;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.maximization.ExplorationExploitationAlgorithm;
import uk.ac.ox.oxfish.utility.maximization.Sensor;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * A simple exploration-imitaiton-exploitation decision where the random part occurs by choosing from a list
 * Created by carrknight on 8/6/15.
 */
public class DiscreteRandomAlgorithm<T> implements ExplorationExploitationAlgorithm<T>
{


    private final Function<Pair<T,MersenneTwisterFast>, T> randomChooser;


    public DiscreteRandomAlgorithm(
            List<T> randomChoices) {
        this.randomChooser = pair -> {
            if(randomChoices.isEmpty()) //if there is nothing randomizable, don't bother
                return pair.getFirst();
            //otherwise randomize!
            return randomChoices.get(pair.getSecond().nextInt(randomChoices.size()));
        };
    }

    @Override
    public T randomize(
            MersenneTwisterFast random, Fisher agent, double currentFitness, T current) {
        return randomChooser.apply(new Pair<T, MersenneTwisterFast>(current,random));
    }


    @Override
    public T imitate(
            MersenneTwisterFast random, Fisher agent, double fitness, T current, Collection<Fisher> friends,
            ObjectiveFunction<Fisher> objectiveFunction, Sensor<T> sensor) {
        return FishStateUtilities.imitateFriendAtRandom(random, fitness,
                                                        current, friends,
                                                        objectiveFunction, sensor);


    }


    @Override
    public T exploit(MersenneTwisterFast random, Fisher agent, double currentFitness, T current) {
        return current; //nothing happens
    }


    @Override
    public T judgeRandomization(
            MersenneTwisterFast random, Fisher agent, double previousFitness, double currentFitness, T current,
            T previous) {
        return current;
    }

    @Override
    public void start(FishState model, Fisher agent) {
        //nothing, no need for a setup
    }
}
