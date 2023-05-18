package uk.ac.ox.oxfish.fisher.log.timeScalarFunctions;

/**
 * Abstract class for time scalar functions.
 * At the moment there are only 3 in the family, but who knows what the future will hold.
 *
 * @author Brian Powers 5/3/2019
 */
public interface TimeScalarFunction {

    double timeScalar(double t);
}
