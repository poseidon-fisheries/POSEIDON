package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.maximization.generic.ParameterAddress;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.parameters.BooleanParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedParameter;

import static com.google.common.base.Preconditions.checkArgument;

public class BooleanParameterAdaptor extends FixedParameterAdaptor<Boolean> {
    BooleanParameterAdaptor(
        final FixedParameter<Boolean> parameter,
        final String name,
        final Scenario scenario
    ) {
        super(parameter, name, scenario);
    }

    @Override
    public void setValue(final Object value) {
        checkArgument(
            value instanceof Boolean,
            "Value of parameter %s needs to be of type Boolean but got %s",
            getName(),
            value.getClass()
        );
        new ParameterAddress(getName())
            .getSetter(scenario)
            .accept(new BooleanParameter((Boolean) value));
    }
}
