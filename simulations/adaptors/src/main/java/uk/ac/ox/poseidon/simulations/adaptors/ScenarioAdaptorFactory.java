package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.poseidon.common.AdaptorFactory;

public interface ScenarioAdaptorFactory<D extends uk.ac.ox.oxfish.model.scenario.Scenario>
    extends AdaptorFactory<D, uk.ac.ox.poseidon.simulations.api.Scenario> {

    @SuppressWarnings("unchecked")
    static <D extends uk.ac.ox.oxfish.model.scenario.Scenario> ScenarioAdaptorFactory<D> loadFor(
        final D scenario
    ) {
        return (ScenarioAdaptorFactory<D>) AdaptorFactory.loadFor(
            ScenarioAdaptorFactory.class,
            scenario.getClass()
        );
    }

}
