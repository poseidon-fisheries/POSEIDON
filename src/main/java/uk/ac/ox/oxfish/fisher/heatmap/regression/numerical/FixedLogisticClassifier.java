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
public class FixedLogisticClassifier {


    /**
     * for each observation extractor (basically for each x) this stores the betas associated with it
     */
    private final LinkedHashMap<ObservationExtractor,Double> betas;


    public FixedLogisticClassifier(Pair<ObservationExtractor, Double>... betas) {

        this.betas= new LinkedHashMap<>();
        for(Pair<ObservationExtractor, Double> beta : betas)
            this.betas.put(beta.getFirst(),beta.getSecond());
    }

    public FixedLogisticClassifier(
            LinkedHashMap<ObservationExtractor, Double> betas) {
        this.betas = betas;
    }

    public boolean test(Fisher agent, FishState model, SeaTile tile, MersenneTwisterFast random){

        double linearComponent = 0;
        for(Map.Entry<ObservationExtractor,Double> element : betas.entrySet())
        {
            linearComponent += element.getKey().extract(tile,model.getHoursSinceStart(),agent,model) * element.getValue();
        }

        return random.nextBoolean(1d/(1+Math.exp(-linearComponent)));
    }

}
