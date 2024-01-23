package uk.ac.ox.oxfish.model.data.monitors.accumulators;

import com.google.common.primitives.ImmutableDoubleArray;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class KsTestAccumulator implements Accumulator<Double> {

    // having this be static means that we share a single RNG, but this
    // should not matter as long as we only use deterministic methods
    private static final KolmogorovSmirnovTest kolmogorovSmirnovTest =
        new KolmogorovSmirnovTest();

    private final ImmutableDoubleArray.Builder arrayBuilder = ImmutableDoubleArray.builder();
    private final Map<Integer, double[]> referenceDistributionPerYear;

    public KsTestAccumulator(final Map<Integer, double[]> referenceDistributionPerYear) {
        this.referenceDistributionPerYear =
            referenceDistributionPerYear
                .entrySet()
                .stream()
                .collect(toImmutableMap(
                    Entry::getKey,
                    entry -> entry.getValue().clone()
                ));
    }

    @Override
    public String getNameFormat() {
        return "K.-S. test statistic for %s";
    }

    @Override
    public void accumulate(final Double value) {
        arrayBuilder.add(value);
    }

    @Override
    public double applyAsDouble(final FishState fishState) {
        return kolmogorovSmirnovTest.kolmogorovSmirnovStatistic(
            arrayBuilder.build().toArray(),
            referenceDistributionPerYear.get(fishState.getCalendarYear())
        );
    }
}
