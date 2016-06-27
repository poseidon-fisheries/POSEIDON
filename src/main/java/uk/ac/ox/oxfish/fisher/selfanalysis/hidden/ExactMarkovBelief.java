package uk.ac.ox.oxfish.fisher.selfanalysis.hidden;

import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.List;

/**
 * The hidden markov chain belief about the state of the world. For this object the whole map is represented.
 * Right now refers exclusively to maps, might
 * make it more general in the future.
 * Created by carrknight on 6/27/16.
 */
public class ExactMarkovBelief implements MarkovBelief
{


    public final DoubleGrid2D belief;

    /**
     * creates a new random uniform markof belief!
     * @param map
     */
    public ExactMarkovBelief(NauticalMap map) {

        belief = new DoubleGrid2D(map.getWidth(),map.getHeight(),0d);
        List<SeaTile> seaTiles = map.getAllSeaTilesExcludingLandAsList();
        double initialAssumption = seaTiles.size()/((double)map.getWidth()*map.getHeight());
        for(SeaTile seaTile : seaTiles)
            belief.set(seaTile.getGridX(),seaTile.getGridY(),initialAssumption);
    }


    /**
     * creates an exact markov belief object from this grid
     */
    public ExactMarkovBelief(DoubleGrid2D belief) {

        this.belief=belief;
    }



    /**
     * returns the belief at this location
     * @return the belief (a number)
     */
    public double getBelief(int x, int y)
    {
        return belief.get(x,y);
    }

    /**
     * returns the belief on this tile
     * @param tile tile
     * @return the belief (a number)
     */
    public double getBelief(SeaTile tile)
    {
        return belief.get(tile.getGridX(),tile.getGridY());
    }

    /**
     * returns the whole grid. Do not modify directly!
     */
    public DoubleGrid2D representBeliefs()
    {
        return belief;
    }
}
