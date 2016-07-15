package uk.ac.ox.oxfish.fisher.heatmap.acquisition;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicalRegression;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Object that picks a "best" from a geographical regression object
 * Created by carrknight on 6/28/16.
 */
public interface AcquisitionFunction
{

    /**
     * The acquisition function main task: to pick a tile from the map given geographical regression
     * @param map the map to pick from
     * @param regression the geographical regression
     * @param state  @return a choice
     * @param fisher
     * @param current
     */
    public SeaTile pick(
            NauticalMap map, GeographicalRegression regression, FishState state, Fisher fisher, SeaTile current);



}
