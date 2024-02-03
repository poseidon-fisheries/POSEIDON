package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.poseidon.common.core.parameters.FixedParameter;

public abstract class FixedParameterAdaptor<T> extends ParameterAdaptor<FixedParameter<T>> {
    FixedParameterAdaptor(
        final FixedParameter<T> parameter,
        final String name,
        final Scenario scenario
    ) {
        super(parameter, name, scenario);
    }

    @Override
    public Object getValue() {
        return getDelegate().getValue();
    }
}
