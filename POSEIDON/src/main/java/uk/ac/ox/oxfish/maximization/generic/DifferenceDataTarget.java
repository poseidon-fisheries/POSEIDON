package uk.ac.ox.oxfish.maximization.generic;

class DifferenceDataTarget extends LastStepFixedDataTarget {
    private static final long serialVersionUID = 794455381901196129L;

    @SuppressWarnings("unused")
    public DifferenceDataTarget() {
        super(new Difference());
    }

    @SuppressWarnings("WeakerAccess")
    public DifferenceDataTarget(
        final String columnName,
        final double fixedTarget
    ) {
        super(new Difference(), columnName, fixedTarget);
    }
}
