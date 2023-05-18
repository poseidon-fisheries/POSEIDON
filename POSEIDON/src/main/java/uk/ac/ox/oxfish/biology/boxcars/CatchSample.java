package uk.ac.ox.oxfish.biology.boxcars;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

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


    public CatchSample(Species species, double[][] landings) {
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
        Species species,
        double[][] landings
    ) {

        return convertLandingsToAbundance(
            new Function<Pair<Integer, Integer>, Double>() {
                @Override
                public Double apply(Pair<Integer, Integer> subdivisionBin) {
                    return species.getWeight(subdivisionBin.getFirst(), subdivisionBin.getSecond());
                }
            },
            landings
        );


    }

    /**
     * Needs to be stepped on the outside. Observes daily landings.
     * supposedly what you'd step on: looks at all the fishers we are sampling
     * and sum up all their landings and ADD it to the abundance vector.
     * Because fishers store their landings in weight, we need a function to turn them back into abundance. Here
     * we use the REAL weight function to do so
     */
    public void observeDaily(Collection<Fisher> observedFishers) {

        for (Fisher fisher : observedFishers) {
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


        return getAbundance(new Function<Pair<Integer, Integer>, Double>() {
            @Override
            public Double apply(Pair<Integer, Integer> subdivisionBin) {
                return species.getWeight(subdivisionBin.getFirst(), subdivisionBin.getSecond());
            }
        });
    }

    public double[][] getAbundance(Function<Pair<Integer, Integer>, Double> subdivisionBinToWeightFunction) {


        return convertLandingsToAbundance(subdivisionBinToWeightFunction, landings);
    }

    /**
     * given landings (in weight, kg most likely) per bin per subdivision, turn those into numbers of fish
     *
     * @param subdivisionBinToWeightFunction function that gives me the weight for each bin and subdivision
     * @param landings                       landings observed
     * @return a double[subdivision][bin] containing NUMBER of fish caught
     */
    public static double[][] convertLandingsToAbundance(
        Function<Pair<Integer, Integer>, Double> subdivisionBinToWeightFunction,
        double[][] landings
    ) {
        double[][] abundanceToReturn = new double[landings.length][landings[0].length];

        for (int subdivision = 0; subdivision < landings.length; subdivision++)
            for (int bin = 0; bin < landings[0].length; bin++) {
                double unitWeight = subdivisionBinToWeightFunction.apply(new Pair<>(subdivision, bin));
                // assumedVarA/1000 * Math.pow(species.getLength(subdivision,bin), assumedVarB);
                abundanceToReturn[subdivision][bin] += landings[subdivision][bin] / unitWeight;
            }


        return abundanceToReturn;
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
