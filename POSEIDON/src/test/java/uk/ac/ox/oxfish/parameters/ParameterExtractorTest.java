package uk.ac.ox.oxfish.parameters;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.poseidon.common.api.parameters.Parameter;

import java.util.stream.Stream;

public class ParameterExtractorTest {

    @Test
    public void testGetParameters() {
        final Stream<ParameterExtractor<Parameter>.Parameter> parameters =
            new ParameterExtractor<>(Parameter.class)
                .getParameters(new FlexibleScenario());
        parameters.map(ParameterExtractor.Parameter::getAddress).forEach(System.out::println);
    }
}
