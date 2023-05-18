package uk.ac.ox.oxfish.fisher.log.timeScalarFunctions;

/**
 * The inverse time scalar function. For high values of T, this will scale down fast.
 * But not exponentially fast!
 * It looks like this:
 * |
 * |
 * \
 * \
 * \__
 * \_______
 * \________
 * \______ or something like that!
 *
 * @author Brian Powers 5/3/2019
 */
public class InverseTimeScalar implements TimeScalarFunction {
    double exponent;

    public InverseTimeScalar(double exponent) {
        this.exponent = exponent;
    }

    public double timeScalar(double t) {
        return 1.0 / Math.pow(t + 1, exponent);
    }
}
