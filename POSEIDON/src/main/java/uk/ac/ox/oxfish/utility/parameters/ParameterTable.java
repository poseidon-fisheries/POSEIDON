package uk.ac.ox.oxfish.utility.parameters;

import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

import java.util.Map;

public interface ParameterTable {
    DoubleParameter get(
        int year,
        String parameterName
    );

    Map<String, ? extends DoubleParameter> getParameters(int year);
}
