package uk.ac.ox.oxfish.parameters;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.maximization.generic.BeanParameterAddressBuilder;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.poseidon.common.api.parameters.Parameter;

import java.util.stream.Stream;

public class ParameterExtractorTest {

    @Test
    public void testGetParameters() {
        final Stream<ParameterExtractor.ExtractedParameter> parameters =
            new ParameterExtractor(
                ImmutableSet.of(Number.class, Boolean.class, String.class, Parameter.class),
                BeanParameterAddressBuilder::new
            )
                .getParameters(new FlexibleScenario());
        parameters
            .map(ParameterExtractor.ExtractedParameter::getAddress)
            .forEach(System.out::println);
    }
}
