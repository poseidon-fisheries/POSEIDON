package uk.ac.ox.oxfish.parameters;

import junit.framework.TestCase;
import uk.ac.ox.oxfish.model.scenario.EpoPathPlannerAbundanceScenario;
import uk.ac.ox.oxfish.utility.parameters.Parameter;

import java.util.stream.Stream;

public class ParameterExtractorTest extends TestCase {

    public void testGetParameters() {
        final Stream<ParameterExtractor<Parameter>.Parameter> parameters =
            new ParameterExtractor<>(Parameter.class)
                .getParameters(new EpoPathPlannerAbundanceScenario());
        parameters.map(ParameterExtractor.Parameter::getAddress).forEach(System.out::println);
    }
}