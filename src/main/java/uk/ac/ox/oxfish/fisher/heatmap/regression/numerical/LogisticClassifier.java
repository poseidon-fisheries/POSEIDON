package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Very simple logit regression returning true or false
 * Created by carrknight on 12/1/16.
 */
public class LogisticClassifier {


    /**
     * for each observation extractor (basically for each x) this stores the betas associated with it
     */
    private final LinkedHashMap<ObservationExtractor,Double> betas;


    public LogisticClassifier(Pair<ObservationExtractor, Double>... betas) {

        this.betas= new LinkedHashMap<>();
        for(Pair<ObservationExtractor, Double> beta : betas)
            this.betas.put(beta.getFirst(),beta.getSecond());
    }

    public LogisticClassifier(
            LinkedHashMap<ObservationExtractor, Double> betas) {
        this.betas = betas;
    }

    /**
     * returns the probability 1/(1+exp(-xb))
     * @param agent the fisher
     * @param hoursSinceStart
     * @param model the model
     * @param tile the tile we are considering (can be null)
     * @return a number between 0 and 1
     */
    public double getProbability(
            Fisher agent, final double hoursSinceStart, FishState model, SeaTile tile)
    {
        double linearComponent = 0;
        for(Map.Entry<ObservationExtractor,Double> element : betas.entrySet())
        {
            linearComponent += element.getKey().extract(tile,
                                                        hoursSinceStart,
                                                        agent,
                                                        model) * element.getValue();
        }
        return 1d/(1+Math.exp(-linearComponent));
    }

    /**
     * returns true or false with probability given by the logistic
     * @param agent the agent making the choice
     * @param testTime the hours since start of simulation at which point we want the test to be made
     * @param model the model being used
     * @param tile the tile we are considering (can be null)
     * @param random the randomizer
     * @return true or false
     */
    public boolean test(
            Fisher agent, final double testTime, FishState model, SeaTile tile, MersenneTwisterFast random){

        return random.nextBoolean(getProbability(agent, testTime, model, tile));
    }

    /**
     * returns true or false with probability given by the logistic. It assumes you are asking to test about the current time
     * @param agent the agent making the choice
     * @param model the model being used
     * @param tile the tile we are considering (can be null)
     * @param random the randomizer
     * @return true or false
     */
    public boolean test(
            Fisher agent, FishState model, SeaTile tile, MersenneTwisterFast random){

        return random.nextBoolean(getProbability(agent, model.getHoursSinceStart(), model, tile));
    }


}
