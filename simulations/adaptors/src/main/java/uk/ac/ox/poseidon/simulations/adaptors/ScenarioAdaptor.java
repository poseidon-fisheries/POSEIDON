package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.parameters.ParameterExtractor;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.Adaptor;
import uk.ac.ox.poseidon.simulations.api.Parameter;
import uk.ac.ox.poseidon.simulations.api.Scenario;
import uk.ac.ox.poseidon.simulations.api.Simulation;

import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

public abstract class ScenarioAdaptor<S extends uk.ac.ox.oxfish.model.scenario.Scenario>
    extends Adaptor<S> implements Scenario {

    ScenarioAdaptor(final S delegate) {
        super(delegate);
    }

    @Override
    public Map<String, Parameter> getParameters() {
        final Stream<DoubleParameterAdaptor> doubleParameters =
            new ParameterExtractor<>(DoubleParameter.class)
                .getParameters(getDelegate())
                .map(delegate -> new DoubleParameterAdaptor(delegate, this));

        return doubleParameters
            .collect(toImmutableMap(Parameter::getName, identity()));
    }

    @Override
    public Simulation newSimulation() {
        final FishState fishState = new FishState();
        fishState.setScenario(getDelegate());
        return new FishStateAdaptor(fishState);
    }
}
