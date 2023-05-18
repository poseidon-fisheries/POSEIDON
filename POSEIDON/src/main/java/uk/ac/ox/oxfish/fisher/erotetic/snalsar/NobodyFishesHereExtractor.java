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
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * If anybody last fished here then it's not acceptable (returns -1) otherwise it returns 1
 * Created by carrknight on 6/7/16.
 */
public class NobodyFishesHereExtractor implements SocialAcceptabilityFeatureExtractor<SeaTile> {

    /**
     * Method called to extract the feature from the object toRepresent, given the observer and the overall model
     *
     * @param toRepresent the list of object from which to extract a feature
     * @param model       the model to represent
     * @param fisher
     */
    @Override
    public Map<SeaTile, Double> extractFeature(
        Collection<SeaTile> toRepresent, FishState model, Fisher fisher
    ) {

        if (toRepresent == null || toRepresent.isEmpty())
            return new HashMap<>();

        HashSet<SeaTile> tilesFished = new HashSet<>();
        for (Fisher other : model.getFishers()) //todo memoize this at model level
        {

            //you don't count
            if (fisher == other)
                continue;

            TripRecord lastFinishedTrip = other.getLastFinishedTrip();
            if (lastFinishedTrip != null)
                tilesFished.addAll(lastFinishedTrip.getTilesFished());
        }

        HashMap<SeaTile, Double> toReturn = new HashMap<>();
        for (SeaTile toJudge : toRepresent) {
            toReturn.put(toJudge, tilesFished.contains(toJudge) ? -1d : 1d);
        }

        assert toReturn.size() == toRepresent.size();
        return toReturn;
    }
}
