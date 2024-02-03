package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.parameters.ParameterExtractor;
import uk.ac.ox.oxfish.utility.parameters.BooleanParameter;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.AbstractAdaptor;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;
import uk.ac.ox.poseidon.common.core.parameters.PathParameter;
import uk.ac.ox.poseidon.common.core.parameters.StringParameter;
import uk.ac.ox.poseidon.simulations.api.Parameter;
import uk.ac.ox.poseidon.simulations.api.Scenario;
import uk.ac.ox.poseidon.simulations.api.Simulation;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

public class ScenarioAdaptor
    extends AbstractAdaptor<uk.ac.ox.oxfish.model.scenario.Scenario>
    implements Scenario {

    ScenarioAdaptor(final uk.ac.ox.oxfish.model.scenario.Scenario delegate) {
        super(delegate);
    }

    @Override
    public Map<String, uk.ac.ox.poseidon.simulations.api.Parameter> getParameters() {
        return new ParameterExtractor<>(uk.ac.ox.poseidon.common.api.parameters.Parameter.class)
            .getParameters(getDelegate())
            .map(extractedParameter -> {
                final uk.ac.ox.poseidon.common.api.parameters.Parameter parameter = extractedParameter.getObject();
                if (parameter instanceof DoubleParameter)
                    return new DoubleParameterAdaptor(
                        (DoubleParameter) parameter,
                        extractedParameter.getAddress(),
                        this.getDelegate()
                    );
                else if (parameter instanceof PathParameter)
                    return new PathParameterAdaptor(
                        (PathParameter) parameter,
                        extractedParameter.getAddress(),
                        this.getDelegate()
                    );
                else if (parameter instanceof IntegerParameter)
                    return new IntegerParameterAdaptor(
                        (IntegerParameter) parameter,
                        extractedParameter.getAddress(),
                        this.getDelegate()
                    );
                else if (parameter instanceof BooleanParameter)
                    return new BooleanParameterAdaptor(
                        (BooleanParameter) parameter,
                        extractedParameter.getAddress(),
                        this.getDelegate()
                    );
                else if (parameter instanceof StringParameter)
                    return new StringParameterAdaptor(
                        (StringParameter) parameter,
                        extractedParameter.getAddress(),
                        this.getDelegate()
                    );
                else
                    throw new IllegalStateException(
                        "No parameter adaptor found for " + parameter.getClass()
                    );
            })
            .collect(toImmutableMap(Parameter::getName, identity()));
    }

    @Override
    public Simulation newSimulation() {
        final FishState fishState = new FishState();
        fishState.setScenario(getDelegate());
        return new FishStateAdaptor(fishState);
    }
}
