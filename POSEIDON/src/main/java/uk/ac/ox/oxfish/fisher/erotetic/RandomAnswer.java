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

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collections;
import java.util.List;

/**
 * Simplest strategy, ignores all representations and just pick at random from all the options
 * Created by carrknight on 4/10/16.
 */
public class RandomAnswer<T> implements EroteticAnswer<T>
{

    /**
     * ignored
     */
    @Override
    public void start(FishState model) {

    }

    /**
     * ignored
     */
    @Override
    public void turnOff() {

    }

    /**
     * Grabs the list of current options and returns the list of all options that are acceptable
     * @param currentOptions list of options, possibly already filtered by others. It is <b>unmodifiable</b>
     * @param representation
     * @param state          the model   @return a list of acceptable options or null if there is pure indifference among them
     * @param fisher   */
    @Override
    public List<T> answer(
            List<T> currentOptions, FeatureExtractors<T> representation, FishState state, Fisher fisher) {
        Preconditions.checkArgument(!currentOptions.isEmpty());
        if(Log.TRACE)
            Log.trace(" picking a random option");

        return Collections.singletonList(currentOptions.get(state.getRandom().nextInt(currentOptions.size())));
    }
}
