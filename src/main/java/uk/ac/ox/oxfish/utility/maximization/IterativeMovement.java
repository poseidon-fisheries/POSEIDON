package uk.ac.ox.oxfish.utility.maximization;

import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * An interface for any iterative algorithm that changes sea-tiles by trial and error
 * Created by carrknight on 6/17/15.
 */
public interface IterativeMovement
{

    /**
     * decide a new tile to move to given the current and previous step and their fitness
     * @param previous the sea-tile tried before this one. Could be null
     * @param current the sea-tile just tried
     * @param previousFitness the fitness value associated with the old sea-tile, could be NaN
     * @param newFitness the fitness value associated with the current tile
     * @return a new sea-tile to try
     */
    SeaTile adapt(SeaTile previous, SeaTile current, double previousFitness, double newFitness);


}
