package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.poseidon.common.core.parameters.FixedParameter;

public class StringParameterAdaptor extends FixedParameterAdaptor<String> {

    StringParameterAdaptor(
        final FixedParameter<String> parameter,
        final String name,
        final Scenario scenario
    ) {
        super(parameter, name, scenario);
    }

    @Override
    public void setValue(final Object value) {
        getDelegate().setValue(value.toString());
    }
}
