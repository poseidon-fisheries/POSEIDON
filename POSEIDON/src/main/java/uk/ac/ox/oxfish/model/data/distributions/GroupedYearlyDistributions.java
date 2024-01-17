package uk.ac.ox.oxfish.model.data.distributions;

import java.util.Map;
import java.util.function.Function;

public interface GroupedYearlyDistributions extends Function<String, Map<Integer, double[]>> {
}
