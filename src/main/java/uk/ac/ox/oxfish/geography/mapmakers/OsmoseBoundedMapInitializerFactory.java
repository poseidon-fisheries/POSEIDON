package uk.ac.ox.oxfish.geography.mapmakers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Creates the Osmose Map Initializer with
 * geographical coordinates
 * Created by carrknight on 11/17/16.
 */
public class OsmoseBoundedMapInitializerFactory implements AlgorithmFactory<OsmoseMapInitializer>
{


    private double  lowRightEasting = 584600.702 ;

    private double lowRightNorthing = 2791787.489 ;

    private double upLeftEasting = -73291.664;

    private double upLeftNorthing = 3445097.299 ;



    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public OsmoseMapInitializer apply(FishState fishState) {
        return new OsmoseMapInitializer(lowRightEasting,lowRightNorthing,
                                        upLeftEasting,upLeftNorthing);
    }

    /**
     * Getter for property 'lowRightEasting'.
     *
     * @return Value for property 'lowRightEasting'.
     */
    public double getLowRightEasting() {
        return lowRightEasting;
    }

    /**
     * Getter for property 'lowRightNorthing'.
     *
     * @return Value for property 'lowRightNorthing'.
     */
    public double getLowRightNorthing() {
        return lowRightNorthing;
    }

    /**
     * Getter for property 'upLeftEasting'.
     *
     * @return Value for property 'upLeftEasting'.
     */
    public double getUpLeftEasting() {
        return upLeftEasting;
    }

    /**
     * Getter for property 'upLeftNorthing'.
     *
     * @return Value for property 'upLeftNorthing'.
     */
    public double getUpLeftNorthing() {
        return upLeftNorthing;
    }

    /**
     * Setter for property 'lowRightEasting'.
     *
     * @param lowRightEasting Value to set for property 'lowRightEasting'.
     */
    public void setLowRightEasting(double lowRightEasting) {
        this.lowRightEasting = lowRightEasting;
    }

    /**
     * Setter for property 'lowRightNorthing'.
     *
     * @param lowRightNorthing Value to set for property 'lowRightNorthing'.
     */
    public void setLowRightNorthing(double lowRightNorthing) {
        this.lowRightNorthing = lowRightNorthing;
    }

    /**
     * Setter for property 'upLeftEasting'.
     *
     * @param upLeftEasting Value to set for property 'upLeftEasting'.
     */
    public void setUpLeftEasting(double upLeftEasting) {
        this.upLeftEasting = upLeftEasting;
    }

    /**
     * Setter for property 'upLeftNorthing'.
     *
     * @param upLeftNorthing Value to set for property 'upLeftNorthing'.
     */
    public void setUpLeftNorthing(double upLeftNorthing) {
        this.upLeftNorthing = upLeftNorthing;
    }
}
