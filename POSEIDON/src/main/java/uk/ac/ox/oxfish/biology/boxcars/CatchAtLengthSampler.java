package uk.ac.ox.oxfish.biology.boxcars;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.Startable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * basically a CatchSample that keeps track of which boats to observe
 */
public interface CatchAtLengthSampler extends Startable {

    /**
     * Step to call (from the outside!) to tell the sampler to look at this day's data
     * Because fishers store their landings in weight, we need a function to turn them back into abundance. Here
     * we use the REAL weight function to do so
     */
    void observeDaily();

    /**
     * when we need to zero the abundance array, call this.
     */
    void resetCatchObservations();

    /**
     * get approximate NUMBER of fish recorded as caught
     */
    double[][] getAbundance();

    /**
     * get approximate NUMBER of fish recorded as caught using a custom function matching bin weight to numbers
     * (this is useful if we don't want to use the REAL species parameters)
     */
    double[][] getAbundance(Function<Map.Entry<Integer, Integer>, Double> subdivisionBinToWeightFunction);


    Species getSpecies();

    double[][] getLandings();

    /**
     * returns unmodifiable list showing fishers
     *
     * @return
     */
    List<Fisher> viewObservedFishers();

}
