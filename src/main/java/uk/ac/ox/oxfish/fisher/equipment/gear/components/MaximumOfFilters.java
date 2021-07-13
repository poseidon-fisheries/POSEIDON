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
            allFilters[filter]= componentFilters[filter].filter(species,arrayCopy(abundance));
        }


        //for each cell, get the max
        for (int subdivision = 0; subdivision < maximumFiltered.length; subdivision++) {
            for (int bin = 0; bin < maximumFiltered[0].length; bin++) {
                maximumFiltered[subdivision][bin] = allFilters[0][subdivision][bin];
                for (int filter = 1; filter < allFilters.length; filter++) {
                    maximumFiltered[subdivision][bin] =
                            Math.max(allFilters[filter][subdivision][bin],
                            maximumFiltered[subdivision][bin]);
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
