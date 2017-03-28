package uk.ac.ox.oxfish.fisher.selfanalysis.profit;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Cost function representing petrol consumed
 * Created by carrknight on 7/13/16.
 */
public class GasCost implements Cost{

    /**
     * computes and return the cost
     *  @param fisher  agent that did the trip
     * @param model
     * @param record  the trip record
     * @param revenue revenue from catches
     * @param durationInHours
     * @return $ spent
     */
    @Override
    public double cost(Fisher fisher, FishState model, TripRecord record, double revenue, double durationInHours) {
        return record.getLitersOfGasConsumed() * fisher.getHomePort().getGasPricePerLiter();
    }


    /**
     //gas consumption = movement + fishing
     double price = fisher.getHomePort().getGasPricePerLiter();
     double movement = price * fisher.getBoat().expectedFuelConsumption(record.getDistanceTravelled());
     double fishing = 0;
     for(Map.Entry<SeaTile,Integer> location :  record.getTilesFishedPerHour())
     fishing+= fisher.getGear().getFuelConsumptionPerHourOfFishing(fisher,fisher.getBoat(),location.getKey()) *
     location.getValue();
     return movement + (fishing * price);
     */
}
