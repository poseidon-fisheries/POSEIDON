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

package uk.ac.ox.oxfish.fisher.strategies.departing;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * The fisher must spend x hours at port before going back out
 * Created by carrknight on 8/11/15.
 */
public class FixedRestTimeDepartingStrategy implements DepartingStrategy
{


    private final double minimumHoursToWait;

    public FixedRestTimeDepartingStrategy(double minimumHoursToWait) {
        Preconditions.checkArgument(minimumHoursToWait >= 0);
        this.minimumHoursToWait = minimumHoursToWait;
    }

    /**
     * The fisher asks himself if he wants to leave the warm comfort of his bed.
     *
     * @return true if the fisherman wants to leave port.
     */
    @Override
    public boolean shouldFisherLeavePort(
            Fisher fisher, FishState model, MersenneTwisterFast random) {
        return fisher.getHoursAtPort() >= minimumHoursToWait;
    }

    /**
     */
    @Override
    public void start(FishState model,Fisher fisher) {

    }

    /**
     * @param fisher
     */
    @Override
    public void turnOff(Fisher fisher) {
//nothing
    }
}
