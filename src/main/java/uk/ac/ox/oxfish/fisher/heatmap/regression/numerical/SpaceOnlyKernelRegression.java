package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

/**
 * Predicts through a kernel (logistic) average.
 * Created by carrknight on 6/27/16.
 */
public class SpaceOnlyKernelRegression extends   AbstractKernelRegression
{





    public SpaceOnlyKernelRegression(int maximumNumberOfObservations,
                                     double distanceBandwidth) {

        super(maximumNumberOfObservations,distanceBandwidth);
    }


    @Override
    protected double distance(double fromX, double fromY, double fromTime, double toX, double toY, double toTime) {
        return Math.sqrt(Math.pow(fromX-toX,2) + Math.pow(fromY-toY,2));
    }

}
