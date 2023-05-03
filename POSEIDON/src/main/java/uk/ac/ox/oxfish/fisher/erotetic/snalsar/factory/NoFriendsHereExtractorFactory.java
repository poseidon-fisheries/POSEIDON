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

package uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory;

import uk.ac.ox.oxfish.fisher.erotetic.snalsar.NoFriendsHereExtractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Created by carrknight on 5/31/16.
 */
public class NoFriendsHereExtractorFactory implements AlgorithmFactory<NoFriendsHereExtractor>
{


    /**
     * when false the fisher will ALSO consider the locations of people who call this fisher a friend (even if the feeling
     * isn't mutual)
     */
    private boolean onlyDirectedLinks = true;

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public NoFriendsHereExtractor apply(FishState state) {
        return new NoFriendsHereExtractor(onlyDirectedLinks);
    }

    /**
     * Getter for property 'onlyDirectedLinks'.
     *
     * @return Value for property 'onlyDirectedLinks'.
     */
    public boolean isOnlyDirectedLinks() {
        return onlyDirectedLinks;
    }

    /**
     * Setter for property 'onlyDirectedLinks'.
     *
     * @param onlyDirectedLinks Value to set for property 'onlyDirectedLinks'.
     */
    public void setOnlyDirectedLinks(boolean onlyDirectedLinks) {
        this.onlyDirectedLinks = onlyDirectedLinks;
    }
}
