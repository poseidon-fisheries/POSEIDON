package uk.ac.ox.oxfish.model.scenario;

import junit.framework.TestCase;
import uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter;
import uk.ac.ox.oxfish.parameters.ParameterExtractor;

import java.util.List;
import java.util.stream.Collectors;

public class EpoPathPlannerAbundanceScenarioTest extends TestCase {

    public void testParameterAnnotationReading() {
        final EpoPathPlannerAbundanceScenario scenario = new EpoPathPlannerAbundanceScenario();
        final List<HardEdgeOptimizationParameter> freeParameters =
            new ParameterExtractor()
                .getParameters(scenario)
                .collect(Collectors.toList());
        freeParameters.forEach(System.out::println);
    }

}