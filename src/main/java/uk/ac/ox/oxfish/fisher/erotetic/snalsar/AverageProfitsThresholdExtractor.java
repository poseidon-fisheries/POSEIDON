/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

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
