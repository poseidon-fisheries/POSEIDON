package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

/**
 * The distance (as in opposite of similiarity) between two observations extracted during a regression
 * Created by carrknight on 8/24/16.
 */
public interface RegressionDistance {


    public double distance(double firstObservation, double secondObservation);


    public double getBandwidth();


    public void setBandwidth(double bandwidth);

}
