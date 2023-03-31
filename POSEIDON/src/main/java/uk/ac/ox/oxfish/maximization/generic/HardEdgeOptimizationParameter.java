package uk.ac.ox.oxfish.maximization.generic;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.parameters.FreeParameter;

/**
 * this is just like simple optimization parameter, but it has additional minimum and maximum which are never
 * crossed even if the EVA optimization allows for bounds to go beyond their usual -10,10 co-domain
 */
public class HardEdgeOptimizationParameter extends SimpleOptimizationParameter {

    private double hardMinimum = Integer.MIN_VALUE;
    private double hardMaximum = Integer.MAX_VALUE;

    public HardEdgeOptimizationParameter(
        final String addressToModify,
        final FreeParameter freeParameter
    ) {
        this(
            addressToModify,
            Double.isNaN(freeParameter.minimum()) ? freeParameter.hardMinimum() : freeParameter.minimum(),
            Double.isNaN(freeParameter.maximum()) ? freeParameter.hardMaximum() : freeParameter.maximum(),
            freeParameter.hardMinimum() >= 0,
            false,
            freeParameter.hardMinimum(),
            freeParameter.hardMaximum()
        );
    }

    public HardEdgeOptimizationParameter(
        final String addressToModify,
        final double minimum,
        final double maximum,
        final boolean alwaysPositive,
        final boolean isRawNumber,
        final double hardMinimum,
        final double hardMaximum
    ) {
        super(addressToModify, minimum, maximum, alwaysPositive, isRawNumber);
        this.hardMinimum = hardMinimum;
        this.hardMaximum = hardMaximum;
    }


    public HardEdgeOptimizationParameter() {
    }


    @Override
    public double computeNumericValue(final double input) {

        Preconditions.checkArgument(
            hardMinimum < hardMaximum,
            super.getAddressToModify() + "has hard edges that are inconsistent"
        );

        final double original = super.computeNumericValue(input);
        if (original < hardMinimum)
            return hardMinimum;
        if (original > hardMaximum)
            return hardMaximum;
        return original;
    }


    public double getHardMinimum() {
        return hardMinimum;
    }

    public void setHardMinimum(final double hardMinimum) {
        this.hardMinimum = hardMinimum;
    }

    public double getHardMaximum() {
        return hardMaximum;
    }

    public void setHardMaximum(final double hardMaximum) {
        this.hardMaximum = hardMaximum;
    }

    @Override
    public String toString() {
        return "HardEdgeOptimizationParameter{" +
            "addressToModify='" + getAddressToModify() + '\'' +
            ", minimum=" + getMinimum() +
            ", maximum=" + getMaximum() +
            ", alwaysPositive=" + isAlwaysPositive() +
            ", isRawNumber=" + isRawNumber() +
            ", hardMinimum=" + hardMinimum +
            ", hardMaximum=" + hardMaximum +
            '}';
    }
}
