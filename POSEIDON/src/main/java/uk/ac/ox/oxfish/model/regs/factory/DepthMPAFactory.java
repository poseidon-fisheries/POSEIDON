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

package uk.ac.ox.oxfish.model.regs.factory;

import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 6/28/17.
 */
public class DepthMPAFactory implements AlgorithmFactory<ProtectedAreasOnly> {


    @SuppressWarnings("deprecation")
    private final uk.ac.ox.oxfish.utility.Locker<String, ProtectedAreasOnly> locker =
        new uk.ac.ox.oxfish.utility.Locker<>();
    private DoubleParameter minDepth = new FixedDoubleParameter(0);
    private DoubleParameter maxDepth = new FixedDoubleParameter(200);

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ProtectedAreasOnly apply(final FishState state) {

        return locker.presentKey(
            state.getUniqueID(),
            () -> {

                //go through the map and put an mpa in each area that qualifies

                final double minDepth = -getMinDepth().applyAsDouble(state.getRandom());
                final double maxDepth = -getMaxDepth().applyAsDouble(state.getRandom());
                for (final SeaTile tile : state.getMap().getAllSeaTilesAsList()) {
                    if (tile.getAltitude() <= minDepth && tile.getAltitude() >= maxDepth) {
                        tile.assignMpa(new MasonGeometry());
                    }
                }

                return new ProtectedAreasOnly();


            }
        );


    }

    /**
     * Getter for property 'minDepth'.
     *
     * @return Value for property 'minDepth'.
     */
    public DoubleParameter getMinDepth() {
        return minDepth;
    }

    /**
     * Setter for property 'minDepth'.
     *
     * @param minDepth Value to set for property 'minDepth'.
     */
    public void setMinDepth(final DoubleParameter minDepth) {
        this.minDepth = minDepth;
    }

    /**
     * Getter for property 'maxDepth'.
     *
     * @return Value for property 'maxDepth'.
     */
    public DoubleParameter getMaxDepth() {
        return maxDepth;
    }

    /**
     * Setter for property 'maxDepth'.
     *
     * @param maxDepth Value to set for property 'maxDepth'.
     */
    public void setMaxDepth(final DoubleParameter maxDepth) {
        this.maxDepth = maxDepth;
    }
}
