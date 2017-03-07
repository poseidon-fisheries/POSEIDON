package uk.ac.ox.oxfish.fisher.heatmap.regression.basis;

import com.google.common.base.Preconditions;

/**
 * The main component of an RBF network. Basically a dot in a space which is used
 * to weigh observations around it when making predictions
 * Created by carrknight on 3/7/17.
 */
public class RBFBasis {

    /**
     * the center of the basis
     */
    private final double[] center;

    /**
     * the bandwidth (or variance)
     */
    private double bandwidth;

    /**
     * 1/sqrt(2*Pi*bandwidth)
     */
    private double multiplier;


    /**
     * builds a basis
     * @param bandwidth
     * @param center
     */
    public RBFBasis(double bandwidth, double... center) {
        this.center = center;
        this.bandwidth = bandwidth;
        this.multiplier = 1d/Math.sqrt(2*Math.PI*bandwidth);
    }

    /**
     * checks kernel between basis and observation
     * @param observation the observation
     * @return a value of similiarity between the center and the observation.
     * The higher the more important this basis is
     */
    public double evaluate(double[] observation)
    {
        double norm = normSquared(center,observation);
        if(Double.isNaN(norm))
            throw new RuntimeException("NaN in RBF basis; unexpected!");
        if(!Double.isFinite(norm)) //if distance is "infinite" then return  0
            return 0d;
        else
            return multiplier * Math.exp(-norm/(2*bandwidth));

    }


    /**
     * square distance per element (euclidean norm without square-rooting the end)
     * @return
     */
    public static double normSquared(double[] vector1, double[] vector2)
    {
        Preconditions.checkArgument(vector1.length==vector2.length);
        double sum = 0;
        for(int i= 0; i<vector1.length; i++)
        {
            sum += Math.pow(vector1[i]-vector2[i],2);
            if(!Double.isFinite(sum)) //if you got to infinity, don't bother
                return sum;
        }
        return sum;
    }

    /**
     * Getter for property 'center'.
     *
     * @return Value for property 'center'.
     */
    public double[] getCenter() {
        return center;
    }

    /**
     * Getter for property 'bandwidth'.
     *
     * @return Value for property 'bandwidth'.
     */
    public double getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(double bandwidth) {
        this.bandwidth = bandwidth;
        this.multiplier = 1d/Math.sqrt(2*Math.PI*bandwidth);

    }

    /**
     * Getter for property 'multiplier'.
     *
     * @return Value for property 'multiplier'.
     */
    public double getMultiplier() {
        return multiplier;
    }
}
