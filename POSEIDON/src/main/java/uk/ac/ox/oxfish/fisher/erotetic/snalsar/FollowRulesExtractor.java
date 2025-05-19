/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.erotetic.snalsar;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Properly follow all the rules (am I allowed at sea? am I allowed THERE?)
 * Created by carrknight on 5/26/16.
 */
public class FollowRulesExtractor implements LegalityFeatureExtractor<SeaTile> {

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
        HashMap<SeaTile, Double> toReturn = new HashMap<>();
        for (SeaTile tile : toRepresent) {
            boolean allowed = fisher.isAllowedAtSea() && fisher.isAllowedToFishHere(tile, model);
            toReturn.put(tile, allowed ? 1.0 : -1.0);
        }
        return toReturn;
    }
}
