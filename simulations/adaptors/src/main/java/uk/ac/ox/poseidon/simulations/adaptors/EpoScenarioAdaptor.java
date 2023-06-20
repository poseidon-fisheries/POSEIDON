package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.utility.parameters.PathParameter;

import java.nio.file.Path;

public abstract class EpoScenarioAdaptor<
    B extends LocalBiology,
    S extends EpoScenario<B>
    > extends ScenarioAdaptor<S> {
    EpoScenarioAdaptor(final S scenario) {
        super(scenario);
    }

    @Override
    public Path getInputFolder() {
        return getDelegate().getInputFolder().get();
    }

    @Override
    public void setInputFolder(final Path path) {
        getDelegate().getInputFolder().setPath(new PathParameter(path));
    }
}
