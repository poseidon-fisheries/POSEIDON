package uk.ac.ox.oxfish.fisher.selfanalysis.heatmap;

/**
 * Created by carrknight on 6/28/16.
 */
public class TimeOnlyKernelRegression extends AbstractKernelRegression {
    public TimeOnlyKernelRegression(int maximumNumberOfObservations, double bandwidth) {
        super(maximumNumberOfObservations, bandwidth);
    }

    @Override
    protected double distance(double fromX, double fromY, double fromTime, double toX, double toY, double toTime) {
        return Math.abs(fromTime-toTime);
    }
}
