package uk.ac.ox.oxfish.fisher.log.timeScalarFunctions;

/**
 * The sigmoidal time scalar function.
 * The a parameter controls how soon the function will drop. High values of a means a longer perfect memory window.
 * The b parameter controls how steeply the function will dip when it does. The lower the b is, the less steep.
 * It looks like this:
 * _______
 * \
 * \
 * |
 * |
 * \
 * \_______________ or something like that!
 *
 * @author Brian Powers 5/3/2019
 */
public class SigmoidalTimeScalar implements TimeScalarFunction {
    double a, b;

    public SigmoidalTimeScalar(double a, double b) {
        this.a = a;
        this.b = b;
    }

    public double timeScalar(double t) {
        return (1 + Math.exp(-a)) / (1 + Math.exp(-a + b * t));

    }
}
