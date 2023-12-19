package uk.ac.ox.oxfish.utility.parameters;

import java.util.Map;

public interface ParameterTable {
    DoubleParameter get(
        int year,
        String parameterName
    );

    Map<String, ? extends DoubleParameter> getParameters(int year);
}
