package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Created by carrknight on 4/12/17.
 */
public class FishPriceExtractor implements ObservationExtractor {


    private final Species species;

    public FishPriceExtractor(Species species) {
        this.species = species;
    }


    @Override
    public double extract(
            SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
        return agent.getHomePort().getMarginalPrice(species,agent);
    }
}
