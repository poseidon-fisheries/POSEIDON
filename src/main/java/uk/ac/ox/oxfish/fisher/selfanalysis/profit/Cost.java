package uk.ac.ox.oxfish.fisher.selfanalysis.profit;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A cost computation, to be used by fishers to properly estimate profits
 * Created by carrknight on 7/12/16.
 */
public interface Cost {


    /**
     * computes and return the cost
     * @param fisher agent that did the trip
     * @param model
     *@param record the trip record
     * @param revenue revenue from catches   @return $ spent
     */
    public double cost(
            Fisher fisher, FishState model, TripRecord record, double revenue);

}
