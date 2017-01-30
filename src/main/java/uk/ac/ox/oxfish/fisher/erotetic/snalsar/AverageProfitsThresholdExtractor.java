package uk.ac.ox.oxfish.fisher.erotetic.snalsar;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;
import uk.ac.ox.oxfish.utility.FixedMap;

import java.util.Collection;
import java.util.HashMap;

/**
 * Extracts the fishery average profits from last trip
 * Created by carrknight on 6/7/16.
 */
public class AverageProfitsThresholdExtractor implements ProfitThresholdExtractor<SeaTile>{


    final private double scale;

    public AverageProfitsThresholdExtractor() {
        scale = 1d;
    }

    public AverageProfitsThresholdExtractor(double scale) {
        this.scale = scale;
    }

    @Override
    public HashMap<SeaTile, Double> extractFeature(
            Collection<SeaTile> toRepresent, FishState model, Fisher fisher) {
        double averageProfits = scale  * model.getLatestDailyObservation(
                FishStateDailyTimeSeries.AVERAGE_LAST_TRIP_HOURLY_PROFITS);
        return new FixedMap<SeaTile, Double>(averageProfits,
                                             toRepresent) ;
    }
}
