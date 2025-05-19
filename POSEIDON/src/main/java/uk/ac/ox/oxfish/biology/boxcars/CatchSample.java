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

package uk.ac.ox.oxfish.biology.boxcars;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.function.Function;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

/**
 * the engine of catch-samplers. Keep track of the catch sample, adds obervations, reweighs them and so on
 */
public class CatchSample {


    /**
     * which species are we sampling
     */
    private final Species species;


    /**
     * here we keep the weight of fish that we are tracking!
     */
    private final double[][] landings;


    public CatchSample(final Species species, final double[][] landings) {
        this.species = species;
        this.landings = landings;
    }

    /**
     * given landings (in weight, kg most likely) per bin per subdivision, turn those into numbers of fish
     *
     * @param species  species object from which to reconstruct weight per bin
     * @param landings landings observed
     * @return a double[subdivision][bin] containing NUMBER of fish caught
     */
    public static double[][] convertLandingsToAbundance(
        final Species species,
        final double[][] landings
    ) {

        return convertLandingsToAbundance(
            subdivisionBin -> species.getWeight(subdivisionBin.getKey(), subdivisionBin.getValue()),
            landings
        );


    }

    /**
     * given landings (in weight, kg most likely) per bin per subdivision, turn those into numbers of fish
     *
     * @param subdivisionBinToWeightFunction function that gives me the weight for each bin and subdivision
     * @param landings                       landings observed
     * @return a double[subdivision][bin] containing NUMBER of fish caught
     */
    public static double[][] convertLandingsToAbundance(
        final Function<Entry<Integer, Integer>, Double> subdivisionBinToWeightFunction,
        final double[][] landings
    ) {
        final double[][] abundanceToReturn = new double[landings.length][landings[0].length];

        for (int subdivision = 0; subdivision < landings.length; subdivision++)
            for (int bin = 0; bin < landings[0].length; bin++) {
                final double unitWeight = subdivisionBinToWeightFunction.apply(entry(subdivision, bin));
                // assumedVarA/1000 * Math.pow(species.getLength(subdivision,bin), assumedVarB);
                abundanceToReturn[subdivision][bin] += landings[subdivision][bin] / unitWeight;
            }


        return abundanceToReturn;
    }

    /**
     * Needs to be stepped on the outside. Observes daily landings.
     * supposedly what you'd step on: looks at all the fishers we are sampling
     * and sum up all their landings and ADD it to the abundance vector.
     * Because fishers store their landings in weight, we need a function to turn them back into abundance. Here
     * we use the REAL weight function to do so
     */
    public void observeDaily(final Collection<Fisher> observedFishers) {

        for (final Fisher fisher : observedFishers) {
            for (int subdivision = 0; subdivision < landings.length; subdivision++)
                for (int bin = 0; bin < landings[0].length; bin++) {
                    landings[subdivision][bin] += (fisher.getDailyCounter().getSpecificLandings(species, subdivision,
                        bin
                    ));
                }
        }


    }

    /**
     * when we need to zero the abundance array, call this.
     */
    public void resetCatchObservations() {

        //clear
        for (int subdivision = 0; subdivision < species.getNumberOfSubdivisions(); subdivision++)
            Arrays.fill(landings[subdivision], 0);
    }

    /**
     * Getter for property 'abundance'.
     *
     * @return Value for property 'abundance'.
     */
    public double[][] getAbundance() {


        return getAbundance(subdivisionBin -> species.getWeight(subdivisionBin.getKey(), subdivisionBin.getValue()));
    }

    public double[][] getAbundance(final Function<Entry<Integer, Integer>, Double> subdivisionBinToWeightFunction) {


        return convertLandingsToAbundance(subdivisionBinToWeightFunction, landings);
    }

    /**
     * Getter for property 'species'.
     *
     * @return Value for property 'species'.
     */
    public Species getSpecies() {
        return species;
    }


    /**
     * landings (in weight) per subdivision and bin
     *
     * @return
     */
    public double[][] getLandings() {
        return landings;
    }
}
