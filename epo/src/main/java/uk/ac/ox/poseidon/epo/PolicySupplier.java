package uk.ac.ox.poseidon.epo;

import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;

import java.util.List;
import java.util.function.Supplier;

public interface PolicySupplier extends Supplier<List<Policy<EpoScenario<?>>>> {
}
