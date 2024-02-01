package uk.ac.ox.oxfish.maximization.generic;

public interface FixedDataTargetGenerator {
    String getName();

    FixedDataTarget create(
        final String columnName,
        final double fixedTarget
    );
}
