package uk.ac.ox.oxfish.maximization.generic;

import com.google.auto.service.AutoService;

@AutoService(FixedDataTargetGenerator.class)
public class SmapeDataTargetGenerator implements FixedDataTargetGenerator {
    @Override
    public String getName() {
        return "SMAPE";
    }

    @Override
    public FixedDataTarget create(
        final String columnName,
        final double fixedTarget
    ) {
        return new SmapeDataTarget(columnName, fixedTarget);
    }
}
