package uk.ac.ox.oxfish.model.scenario;

import junit.framework.TestCase;
import uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter;
import uk.ac.ox.oxfish.parameters.ParameterExtractor;

import java.util.List;
import java.util.stream.Collectors;

public class EpoPathPlanningAbundanceScenarioTest extends TestCase {

    public void testParameterAnnotationReading() {
        final EpoPathPlanningAbundanceScenario scenario = new EpoPathPlanningAbundanceScenario();
        final List<HardEdgeOptimizationParameter> freeParameters =
            new ParameterExtractor<>(HardEdgeOptimizationParameter::new)
                .getFreeParameters(scenario)
                .collect(Collectors.toList());
        freeParameters.forEach(System.out::println);
    }


}