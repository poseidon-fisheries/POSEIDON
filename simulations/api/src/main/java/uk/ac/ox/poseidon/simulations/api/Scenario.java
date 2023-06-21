package uk.ac.ox.poseidon.simulations.api;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;


public interface Scenario {
    Simulation newSimulation();

    default Object getParameterValue(final String parameterName) {
        final Map<String, Parameter> parameters = getParameters();
        checkArgument(
            parameters.containsKey(parameterName),
            "Parameter %s not found in scenario.",
            parameterName
        );
        return parameters.get(parameterName).getValue();
    }

    Map<String, Parameter> getParameters();

    default void setParameterValue(final String parameterName, final Object parameterValue) {
        final Map<String, Parameter> parameters = getParameters();
        checkArgument(
            parameters.containsKey(parameterName),
            "Parameter %s not found in scenario.",
            parameterName
        );
        parameters.get(parameterName).setValue(parameterValue);
    }
}