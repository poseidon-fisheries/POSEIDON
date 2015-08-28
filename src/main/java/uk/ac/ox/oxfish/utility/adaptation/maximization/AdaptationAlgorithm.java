package uk.ac.ox.oxfish.utility.adaptation.maximization;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.util.Collection;

/**
 * The algorithmic part of the adaptation routine of agents. There are always 3 possible actions:
 * <ul>
 *     <li> Randomization</li>
 *     <li> Imitation</li>
 *     <li> Exploitation</li>
 * </ul>
 *  <p>
 *  Probabilities are sequential but only one action is ever taken. So if the probabilities of exploration and imitation
 *  are both at 70% then there is a 70% of exploring and a (.3)(.7) probability of imitating. Failing both the agent will
 *  exploit
 *
 * Created by carrknight on 8/6/15.
 */
public interface AdaptationAlgorithm<T> {




    void start(FishState model, Fisher agent, T initial);


    T randomize(
            MersenneTwisterFast random, Fisher agent, double currentFitness,
            T current);

    /**
     * if you have explored in the previous step, this gets called to make you judge exploration (you might want to
     * backtrack). Return null if you don't want to backtrack and skip directly to another round of exploration-exploitation
     */

    T judgeRandomization(
            MersenneTwisterFast random, Fisher agent,
            double previousFitness, double currentFitness,
            T previous, T current);

    T imitate(
            MersenneTwisterFast random,
            Fisher agent, double fitness,
            T current,
            Collection<Fisher> friends,
            ObjectiveFunction<Fisher> objectiveFunction,
            Sensor<T> sensor);


    T exploit(
            MersenneTwisterFast random,
            Fisher agent, double currentFitness,
            T current);


}
