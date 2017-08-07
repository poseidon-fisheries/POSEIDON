package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Returns 1 if the tile is in the same group and 0 otherwise
 * Created by carrknight on 8/7/17.
 */
public class GroupDummyExtractor implements ObservationExtractor{


    /**
     * returns 1 if the tile is in this group and 0 otherwise
     */
    final private int group;

    final private MapDiscretization discretization;


    public GroupDummyExtractor(int group, MapDiscretization discretization) {
        this.group = group;
        this.discretization = discretization;
    }

    @Override
    public double extract(SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
        if(discretization.getGroup(tile)==group)
            return 1;
        else
            return 0;
    }
}
