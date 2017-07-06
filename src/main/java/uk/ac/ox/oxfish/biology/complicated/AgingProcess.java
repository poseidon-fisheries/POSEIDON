package uk.ac.ox.oxfish.biology.complicated;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Ages the abundance-based biomass.
 * Created by carrknight on 7/6/17.
 */
public interface AgingProcess {


    /**
     * as a side-effect ages the local biology according to its rules
     * @param localBiology
     * @param model
     */
    public void ageLocally(AbundanceBasedLocalBiology localBiology,Species species,
                      FishState model);

}
