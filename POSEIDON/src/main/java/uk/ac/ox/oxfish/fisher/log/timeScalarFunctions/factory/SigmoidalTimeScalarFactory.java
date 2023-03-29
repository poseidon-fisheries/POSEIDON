package uk.ac.ox.oxfish.fisher.log.timeScalarFunctions.factory;

import uk.ac.ox.oxfish.fisher.log.timeScalarFunctions.SigmoidalTimeScalar;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * A factory to create the sigmoidal time scalar function for use in the Geralized Cognitive Model
 * to scale the impact of old memories of fishing trips
 *
 * @author Brian Powers on 5/3/2019
 */
public class SigmoidalTimeScalarFactory implements AlgorithmFactory<SigmoidalTimeScalar> {
    private DoubleParameter a = new FixedDoubleParameter(1.0);
    private DoubleParameter b = new FixedDoubleParameter(1.0);

    @Override
    public SigmoidalTimeScalar apply(final FishState state) {
        return new SigmoidalTimeScalar(a.applyAsDouble(state.random), b.applyAsDouble(state.random));
    }

    public DoubleParameter getA() {
        return a;
    }

    public void setA(final DoubleParameter a) {
        this.a = a;
    }

    public DoubleParameter getB() {
        return b;
    }

    public void setB(final DoubleParameter b) {
        this.b = b;
    }

}
