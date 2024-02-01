package uk.ac.ox.oxfish.maximization.generic;

public class SmapeDataTarget extends LastStepFixedDataTarget {

    private static final long serialVersionUID = 8271909309829868639L;

    @SuppressWarnings("unused")
    public SmapeDataTarget() {
        super(new SymmetricMeanAbsolutePercentage());
    }

    @SuppressWarnings("WeakerAccess")
    public SmapeDataTarget(
        final String columnName,
        final double fixedTarget
    ) {
        super(new SymmetricMeanAbsolutePercentage(), columnName, fixedTarget);
    }
}
