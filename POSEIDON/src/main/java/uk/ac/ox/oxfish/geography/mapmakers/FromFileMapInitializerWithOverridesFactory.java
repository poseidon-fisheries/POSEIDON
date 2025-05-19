/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.geography.mapmakers;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.HashBasedTable;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

public class FromFileMapInitializerWithOverridesFactory extends FromFileMapInitializerFactory {


    /**
     * a list of x,y,depth (GRID not latlong) to override the altitude maps...
     */
    private List<String> depthOverrides = Lists.newArrayList("0,0,100");

    public FromFileMapInitializerWithOverridesFactory() {
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FromFileMapInitializer apply(final FishState state) {
        final MersenneTwisterFast rng = state.getRandom();
        final HashBasedTable<Integer, Integer, Double> overrides = HashBasedTable.create(
            depthOverrides.size(),
            depthOverrides.size()
        );
        for (final String override : depthOverrides) {
            final String[] split = override.split(",");
            overrides.put(
                Integer.parseInt(split[0]),
                Integer.parseInt(split[1]),
                Double.parseDouble(split[2])
            );

        }

        return new FromFileMapInitializer(
            getMapFile().get(),
            (int) getGridWidthInCell().applyAsDouble(rng),
            getMapPaddingInDegrees().applyAsDouble(rng),
            isHeader(),
            isLatLong(),
            overrides
        );
    }

    public List<String> getDepthOverrides() {
        return depthOverrides;
    }

    public void setDepthOverrides(final List<String> depthOverrides) {
        this.depthOverrides = depthOverrides;
    }
}
