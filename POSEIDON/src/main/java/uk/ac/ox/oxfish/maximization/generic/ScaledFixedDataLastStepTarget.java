package uk.ac.ox.oxfish.maximization.generic;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.abs;

public class ScaledFixedDataLastStepTarget extends AbstractLastStepFixedDataTarget {

    private static final long serialVersionUID = 1135798739814067783L;

    public ScaledFixedDataLastStepTarget() {
        super((observed, predicted) -> abs((observed - predicted) / observed), 1, false);
    }

    public void setFixedTarget(final double fixedTarget) {
        checkArgument(fixedTarget != 0);
        super.setFixedTarget(fixedTarget);
    }

}
