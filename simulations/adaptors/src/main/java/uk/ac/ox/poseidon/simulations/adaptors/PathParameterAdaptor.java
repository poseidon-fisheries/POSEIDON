package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.parameters.FixedParameter;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathParameterAdaptor extends FixedParameterAdaptor<Path> {
    public PathParameterAdaptor(
        final FixedParameter<Path> parameter,
        final String name,
        final Scenario scenario
    ) {
        super(parameter, name, scenario);
    }

    @Override
    public void setValue(final Object value) {
        getDelegate().setValue(Paths.get(value.toString()));
    }
}
