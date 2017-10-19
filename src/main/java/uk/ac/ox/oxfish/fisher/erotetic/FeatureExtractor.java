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
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.Map;

/**
 * A function used by agents to extract/represent a feature of the object of class T they want to examine
 * Created by carrknight on 4/10/16.
 */
public interface  FeatureExtractor<T> {


    /**
     * Method called to extract the feature from the object toRepresent, given the observer and the overall model
     * @param toRepresent the list of object from which to extract a feature
     * @param model the model to represent
     * @param fisher
     * */
    Map<T,Double> extractFeature(
            Collection<T> toRepresent, FishState model, Fisher fisher);




    //common names:
    String AVERAGE_PROFIT_FEATURE = "Average Fishery Profits Per Trip";


}
