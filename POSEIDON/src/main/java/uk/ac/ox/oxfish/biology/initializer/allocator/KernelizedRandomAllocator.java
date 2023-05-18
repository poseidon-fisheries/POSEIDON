/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import ec.util.MersenneTwisterFast;
import io.github.carrknight.heatmaps.regression.KernelNumericalRegression;
import io.github.carrknight.heatmaps.regression.distance.FeatureKernel;
import io.github.carrknight.heatmaps.regression.distance.RBFKernel;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * creates a random map then smooth it with a Kernel regression
 */
public class KernelizedRandomAllocator implements BiomassAllocator {


    private final double maxAllocation;

    private final double minAllocation;

    private final double bandwidth;

    /**
     * how many initial random points should we feed to the kernel regression?
     */
    private final int fixedPoints;

    private KernelNumericalRegression smoother;

    public KernelizedRandomAllocator(
        double maxAllocation, double minAllocation,
        double bandwidth,
        int fixedPoints
    ) {
        this.maxAllocation = maxAllocation;
        this.minAllocation = minAllocation;
        this.bandwidth = bandwidth;
        this.fixedPoints = fixedPoints;
    }

    /**
     * Returns a positive number representing the weight in terms of either
     * biomass or carrying capacity (or whatever else the allocator is used for)
     *
     * @param tile   tile to allocate a weight to
     * @param map    general map information
     * @param random
     * @return
     */
    @Override
    public double allocate(
        SeaTile tile, NauticalMap map, MersenneTwisterFast random
    ) {
        //lazily initialize it
        if (smoother == null) {
            smoother = new KernelNumericalRegression(
                new FeatureKernel[]{
                    new RBFKernel(bandwidth),
                    new RBFKernel(bandwidth)
                },
                fixedPoints
            );

            for (int i = 0; i < fixedPoints; i++)
                smoother.observe(
                    new double[]{
                        random.nextDouble() * map.getWidth(),
                        random.nextDouble() * map.getHeight()

                    },
                    random.nextDouble() * (maxAllocation - minAllocation) - minAllocation
                );

        }


        return smoother.predict(
            new double[]{
                tile.getGridX(),
                tile.getGridY()

            }
        );
    }
}
