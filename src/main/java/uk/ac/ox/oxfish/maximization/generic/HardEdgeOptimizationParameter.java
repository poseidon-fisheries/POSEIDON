package uk.ac.ox.oxfish.maximization.generic;

import com.google.common.base.Preconditions;

/**
 * this is just like simple optimization parameter, but it has additional minimum and maximum which are never
 * crossed even if the EVA optimization allows for bounds to go beyond their usual -10,10 co-domain
 */
public class HardEdgeOptimizationParameter extends SimpleOptimizationParameter {


    private double hardMinimum = Integer.MIN_VALUE;

    private double hardMaximum = Integer.MAX_VALUE;


    public HardEdgeOptimizationParameter() {
    }


    @Override
    public double computeNumericValue(double input) {

        Preconditions.checkArgument(hardMinimum<hardMaximum, super.getAddressToModify() + "has hard edges that are inconsistent");

        final double original = super.computeNumericValue(input);
        if(original<hardMinimum)
            return hardMinimum;
        if(original>hardMaximum)
            return hardMaximum;
        return original;
    }


    public double getHardMinimum() {
        return hardMinimum;
    }

    public void setHardMinimum(double hardMinimum) {
        this.hardMinimum = hardMinimum;
    }

    public double getHardMaximum() {
        return hardMaximum;
    }

    public void setHardMaximum(double hardMaximum) {
        this.hardMaximum = hardMaximum;
    }
}
