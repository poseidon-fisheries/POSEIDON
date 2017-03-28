package uk.ac.ox.oxfish.fisher.selfanalysis.profit;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Simple fixed $/hr cost
 * Created by carrknight on 7/13/16.
 */
public class HourlyCost implements Cost{

    private final double hourlyCost;


    public HourlyCost(double hourlyCost) {
        this.hourlyCost = hourlyCost;
    }


    /**
     * computes and return the cost
     *  @param fisher  agent that did the trip
     * @param model
     * @param record  the trip record
     * @param revenue revenue from catches   @return $ spent
     * @param durationInHours
     */
    @Override
    public double cost(
            Fisher fisher, FishState model, TripRecord record, double revenue, double durationInHours) {
        return hourlyCost * durationInHours;
    }
}
