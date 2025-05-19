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

package uk.ac.ox.oxfish.fisher.erotetic;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Simplest strategy, ignores all representations and just pick at random from all the options
 * Created by carrknight on 4/10/16.
 */
public class RandomAnswer<T> implements EroteticAnswer<T> {

    /**
     * ignored
     */
    @Override
    public void start(final FishState model) {

    }

    /**
     * ignored
     */
    @Override
    public void turnOff() {

    }

    /**
     * Grabs the list of current options and returns the list of all options that are acceptable
     *
     * @param currentOptions list of options, possibly already filtered by others. It is <b>unmodifiable</b>
     * @param representation
     * @param state          the model   @return a list of acceptable options or null if there is pure indifference among them
     * @param fisher
     */
    @Override
    public List<T> answer(
        final List<T> currentOptions,
        final FeatureExtractors<T> representation,
        final FishState state,
        final Fisher fisher
    ) {
        Preconditions.checkArgument(!currentOptions.isEmpty());

        Logger.getGlobal().fine(" picking a random option");

        return Collections.singletonList(currentOptions.get(state.getRandom().nextInt(currentOptions.size())));
    }
}
