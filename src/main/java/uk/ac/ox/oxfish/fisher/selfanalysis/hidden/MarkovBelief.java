package uk.ac.ox.oxfish.fisher.selfanalysis.hidden;

import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Created by carrknight on 6/27/16.
 */
public interface MarkovBelief
{


    /**
     * returns the belief at this location
     * @return the belief (a number)
     */
    public double getBelief(int x, int y);

    /**
     * returns the belief on this tile
     * @param tile tile
     * @return the belief (a number)
     */
    public double getBelief(SeaTile tile);

    /**
     * returns the whole grid. Probably not safe for modification
     */
    public DoubleGrid2D representBeliefs();
}
