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

package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * No limits on catch but you can only fish outside of MPAs
 * Created by carrknight on 5/2/15.
 */
public class ProtectedAreasOnly implements Regulation {
    /**
     * can the agent fish at this location?
     *
     * @param agent the agent that wants to fish
     * @param tile  the tile the fisher is trying to fish on
     * @param model
     * @return true if the fisher can fish
     */
    @Override
    public boolean canFishHere(Fisher agent, SeaTile tile, FishState model, int timeStep) {
        return !tile.isProtected();
    }

    /**
     * can always sell
     */
    @Override
    public double maximumBiomassSellable(Fisher agent, Species species, FishState model, int timeStep) {
        return Double.MAX_VALUE;
    }

    /**
     * Can always leave
     */
    @Override
    public boolean allowedAtSea(Fisher fisher, FishState model, int timeStep) {
        return true;
    }

    /**
     * returns a copy of the regulation, used defensively
     *
     * @return
     */
    @Override
    public Regulation makeCopy() {
        return new ProtectedAreasOnly();
    }

}
