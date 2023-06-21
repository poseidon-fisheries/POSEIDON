package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.maximization.generic.ParameterAddress;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.parameters.FixedParameter;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;

import static com.google.common.base.Preconditions.checkArgument;

public class IntegerParameterAdaptor extends FixedParameterAdaptor<Integer> {
    IntegerParameterAdaptor(
        final FixedParameter<Integer> parameter,
        final String name,
        final Scenario scenario
    ) {
        super(parameter, name, scenario);
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
            .accept(new IntegerParameter(((Number) value).intValue()));
    }
}
