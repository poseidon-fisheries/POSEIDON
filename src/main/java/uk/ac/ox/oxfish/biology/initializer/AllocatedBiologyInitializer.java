package uk.ac.ox.oxfish.biology.initializer;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.function.Function;

/**
 * Created by carrknight on 6/15/17.
 */
public interface AllocatedBiologyInitializer extends BiologyInitializer {


    /**
     * puts the function describing the % of biomass that will initially be allocated to this sea-tile
     */
    public Function<SeaTile, Double> putAllocator(
            Species key,
            Function< SeaTile, Double> value);


    /**
     * you must at all time be ready to reset local biology to its pristine state
     * @param species species you want the biomass resetted
     */
    public void resetLocalBiology(Species species);


}
