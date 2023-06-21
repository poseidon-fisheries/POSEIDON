package uk.ac.ox.poseidon.simulations.adaptors;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.maximization.generic.ParameterAddress;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static com.google.common.base.Preconditions.checkArgument;

public class DoubleParameterAdaptor extends ParameterAdaptor<DoubleParameter> {

    private final MersenneTwisterFast rng;

    public DoubleParameterAdaptor(
        final DoubleParameter parameter,
        final String name,
        final Scenario scenario
    ) {
        this(parameter, name, scenario, new MersenneTwisterFast());
    }

    public DoubleParameterAdaptor(
        final DoubleParameter parameter,
        final String name,
        final Scenario scenario,
        final MersenneTwisterFast rng
    ) {
        super(parameter, name, scenario);
        this.rng = rng;
    }

    @Override
    public Double getValue() {
        return getDelegate().applyAsDouble(rng);
    }

    @Override
    public void setValue(final Object value) {
        checkArgument(
            value instanceof Number,
            "Value of parameter %s needs to be of type Number but got %s",
            getName(),
            value.getClass()
        );
        new ParameterAddress(getName())
            .getSetter(scenario)
            .accept(new FixedDoubleParameter(((Number) value).doubleValue()));
    }

}
