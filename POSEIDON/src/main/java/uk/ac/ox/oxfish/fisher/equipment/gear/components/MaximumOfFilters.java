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

package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import uk.ac.ox.oxfish.biology.Species;

/**
 * goes through an array of filters and for each cell of the abundance returns the MAX between the individual filters.
 * This is useful for vulnerability
 */
public class MaximumOfFilters implements AbundanceFilter {


    private final AbundanceFilter[] componentFilters;

    public MaximumOfFilters(AbundanceFilter... componentFilters) {
        this.componentFilters = componentFilters;
    }

    @Override
    public double[][] filter(Species species, double[][] abundance) {
        double[][] maximumFiltered = new double[abundance.length][abundance[0].length];
        double[][][] allFilters = new double[componentFilters.length][][];
        //run all filters
        for (int filter = 0; filter < allFilters.length; filter++) {
            allFilters[filter] = componentFilters[filter].filter(species, arrayCopy(abundance));
        }


        //for each cell, get the max
        for (int subdivision = 0; subdivision < maximumFiltered.length; subdivision++) {
            for (int bin = 0; bin < maximumFiltered[0].length; bin++) {
                maximumFiltered[subdivision][bin] = allFilters[0][subdivision][bin];
                for (int filter = 1; filter < allFilters.length; filter++) {
                    maximumFiltered[subdivision][bin] =
                        Math.max(
                            allFilters[filter][subdivision][bin],
                            maximumFiltered[subdivision][bin]
                        );
                }


            }
        }

        //return to the maximum filtered!
        return maximumFiltered;
    }


    private static double[][] arrayCopy(double[][] source) {
        double[][] output = new double[source.length][source[0].length];
        for (int i = 0; i < source.length; i++) {
            System.arraycopy(source[i], 0, output[i], 0, source[i].length);
        }
        return output;
    }
}
