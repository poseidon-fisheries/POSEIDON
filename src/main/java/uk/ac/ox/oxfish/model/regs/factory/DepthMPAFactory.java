package uk.ac.ox.oxfish.model.regs.factory;

import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.Supplier;

/**
 * Created by carrknight on 6/28/17.
 */
public class DepthMPAFactory  implements AlgorithmFactory<ProtectedAreasOnly>{


    private DoubleParameter minDepth = new FixedDoubleParameter(0);

    private DoubleParameter maxDepth = new FixedDoubleParameter(200);


    private Locker<FishState,ProtectedAreasOnly> locker = new Locker<>();


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ProtectedAreasOnly apply(FishState state) {

        return locker.presentKey(state, new Supplier<ProtectedAreasOnly>() {
            @Override
            public ProtectedAreasOnly get() {

                //go through the map and put an mpa in each area that qualifies

                double minDepth = -getMinDepth().apply(state.getRandom());
                double maxDepth = -getMaxDepth().apply(state.getRandom());
                for(SeaTile tile : state.getMap().getAllSeaTilesExcludingLandAsList())
                {
                    if(tile.getAltitude()<=minDepth && tile.getAltitude()>=maxDepth)
                    {
                        tile.assignMpa(new MasonGeometry());
                    }
                }

                return new ProtectedAreasOnly();


            }
        });


    }

    /**
     * Getter for property 'minDepth'.
     *
     * @return Value for property 'minDepth'.
     */
    public DoubleParameter getMinDepth() {
        return minDepth;
    }

    /**
     * Setter for property 'minDepth'.
     *
     * @param minDepth Value to set for property 'minDepth'.
     */
    public void setMinDepth(DoubleParameter minDepth) {
        this.minDepth = minDepth;
    }

    /**
     * Getter for property 'maxDepth'.
     *
     * @return Value for property 'maxDepth'.
     */
    public DoubleParameter getMaxDepth() {
        return maxDepth;
    }

    /**
     * Setter for property 'maxDepth'.
     *
     * @param maxDepth Value to set for property 'maxDepth'.
     */
    public void setMaxDepth(DoubleParameter maxDepth) {
        this.maxDepth = maxDepth;
    }
}
