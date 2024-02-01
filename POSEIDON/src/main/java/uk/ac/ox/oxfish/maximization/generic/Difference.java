package uk.ac.ox.oxfish.maximization.generic;

import static java.lang.Math.abs;

public class Difference implements ErrorMeasure {
    @Override
    public double applyAsDouble(
        final double target,
        final double result
    ) {
        return abs(result - target);
    }
}
