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
import uk.ac.ox.oxfish.utility.FixedMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by carrknight on 6/7/16.
 */
public class FixedProfitThresholdExtractor implements ProfitThresholdExtractor<SeaTile> {


    private final double threshold;


    public FixedProfitThresholdExtractor(double threshold) {
        this.threshold = threshold;
    }


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
        HashMap<SeaTile, Double> features = new HashMap<>();

        return new FixedMap<>(threshold, toRepresent);
    }

    /**
     * Getter for property 'threshold'.
     *
     * @return Value for property 'threshold'.
     */
    public double getThreshold() {
        return threshold;
    }
}
