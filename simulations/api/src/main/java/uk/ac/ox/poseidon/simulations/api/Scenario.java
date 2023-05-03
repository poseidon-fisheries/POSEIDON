package uk.ac.ox.poseidon.simulations.api;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;


public interface Scenario {
    Simulation newSimulation();

    Path getInputFolder();

    void setInputFolder(Path path);

    default void setInputFolder(final String first, final String... more) {
        setInputFolder(Paths.get(first, more));
    }

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