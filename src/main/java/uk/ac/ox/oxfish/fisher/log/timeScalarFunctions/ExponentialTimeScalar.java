package uk.ac.ox.oxfish.fisher.log.timeScalarFunctions;

/**
 * The exponential time scalar function. For high values of T, this will scale down pretty fast.
 * Exponentially fast, in fact!
 * It looks like this:
 * |
 * |
 * \
 *  \
 *   \_
 *     \_______
 *             \______________ or something like that!
 * @author Brian Powers 5/3/2019
 *
 */

public class ExponentialTimeScalar implements TimeScalarFunction{
	double exponent;
	
	public ExponentialTimeScalar(double exponent){
		this.exponent=exponent;
	}
	
	public double timeScalar(double t){
		return Math.exp(-exponent * t);
	}
}
