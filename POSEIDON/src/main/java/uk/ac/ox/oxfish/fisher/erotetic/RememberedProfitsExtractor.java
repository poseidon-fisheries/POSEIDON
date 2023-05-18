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

package uk.ac.ox.oxfish.fisher.erotetic;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.erotetic.snalsar.ProfitFeatureExtractor;
import uk.ac.ox.oxfish.fisher.log.LocationMemory;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Extract profits from a seatile by checking your memory.
 * Created by carrknight on 4/10/16.
 */
public class RememberedProfitsExtractor implements
    ProfitFeatureExtractor<SeaTile> {


    private final boolean includingOpportunityCosts;

    public RememberedProfitsExtractor(boolean includingOpportunityCosts) {
        this.includingOpportunityCosts = includingOpportunityCosts;
    }


    /**
     * Method called to extract the feature from the object toRepresent, given the observer and the overall model
     *
     * @param toRepresent the list of object from which to extract a feature
     * @param model       the model to represent
     * @param fisher
     * @return a map of toRepresent ---> feature (as double); could be empty or null.
     * For all elements that were present as parameters
     * but not in the output this extractor could not find the correct feature for them.
     */
    @Override
    public HashMap<SeaTile, Double> extractFeature(
        Collection<SeaTile> toRepresent,
        FishState model, Fisher fisher
    ) {

        Map<SeaTile, LocationMemory<TripRecord>> memories = fisher.rememberAllTrips();
        HashMap<SeaTile, Double> features = new HashMap<>();
        for (Map.Entry<SeaTile, LocationMemory<TripRecord>> memory : memories.entrySet()) {
            if (toRepresent.contains(memory.getKey())) {
                double profits = memory.getValue().getInformation().getProfitPerHour(includingOpportunityCosts);
                if (Double.isFinite(profits))
                    features.put(memory.getKey(), profits);
            }

        }
        return features;

    }


}
