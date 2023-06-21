package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.poseidon.common.Adaptor;

public abstract class ParameterAdaptor<P extends uk.ac.ox.oxfish.utility.parameters.Parameter>
    extends Adaptor<P>
    implements uk.ac.ox.poseidon.simulations.api.Parameter {
    final Scenario scenario;
    private final String name;

    ParameterAdaptor(
        final P parameter,
        final String name,
        final Scenario scenario
    ) {
        super(parameter);
        this.name = name;
        this.scenario = scenario;
    }

    @Override
    public String getName() {
        return name;
    }
}
