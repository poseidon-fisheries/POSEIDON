package uk.ac.ox.poseidon.simulations.adaptors;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.maximization.generic.ParameterAddress;
import uk.ac.ox.oxfish.parameters.ParameterExtractor;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.poseidon.common.Adaptor;
import uk.ac.ox.poseidon.simulations.api.Parameter;

import static com.google.common.base.Preconditions.checkArgument;

public class DoubleParameterAdaptor
    extends Adaptor<ParameterExtractor<DoubleParameter>.Parameter>
    implements Parameter {

    private final ScenarioAdaptor<?> scenarioAdaptor;
    private final MersenneTwisterFast rng;


    DoubleParameterAdaptor(
        final ParameterExtractor<DoubleParameter>.Parameter delegate,
        final ScenarioAdaptor<?> scenarioAdaptor
    ) {
        this(delegate, scenarioAdaptor, new MersenneTwisterFast());
    }

    private DoubleParameterAdaptor(
        final ParameterExtractor<DoubleParameter>.Parameter delegate,
        final ScenarioAdaptor<?> scenarioAdaptor, final MersenneTwisterFast rng
    ) {
        super(delegate);
        this.scenarioAdaptor = scenarioAdaptor;
        this.rng = rng;
    }

    @Override
    public Double getValue() {
        return getDelegate().getObject().applyAsDouble(rng);
    }

    @Override
    public void setValue(final Object value) {
        checkArgument(
            value instanceof Double,
            "Value of parameter %s needs to be of type Double but got %s",
            getName(),
            value.getClass()
        );
        new ParameterAddress(getName())
            .getSetter(scenarioAdaptor.getDelegate())
            .accept(new FixedDoubleParameter((Double) value));
    }

    @Override
    public String getName() {
        return getDelegate().getAddress();
    }
}
