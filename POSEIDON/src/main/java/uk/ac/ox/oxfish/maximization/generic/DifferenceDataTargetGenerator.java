package uk.ac.ox.oxfish.maximization.generic;

import com.google.auto.service.AutoService;

@AutoService(FixedDataTargetGenerator.class)
public class DifferenceDataTargetGenerator implements FixedDataTargetGenerator {
    @Override
    public String getName() {
        return "Difference";
    }

    @Override
    public FixedDataTarget create(
        final String columnName,
        final double fixedTarget
    ) {
        return new DifferenceDataTarget(columnName, fixedTarget);
    }
}
