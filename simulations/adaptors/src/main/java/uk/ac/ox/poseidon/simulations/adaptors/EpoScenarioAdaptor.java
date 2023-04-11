package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AggregatingFad;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;

import java.nio.file.Path;

public abstract class EpoScenarioAdaptor<
    B extends LocalBiology,
    F extends AggregatingFad<B, F>,
    S extends EpoScenario<B, F>
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
        getDelegate().getInputFolder().setPath(path);
    }
}
