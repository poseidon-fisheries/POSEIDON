package uk.ac.ox.oxfish.maximization.generic;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.abs;

public class ScaledFixedDataLastStepTarget extends AbstractLastStepFixedDataTarget {

    public ScaledFixedDataLastStepTarget() {
        super((observed, predicted) -> abs((observed - predicted) / observed), 1, false);
    }

    public void setFixedTarget(double fixedTarget) {
        checkArgument(fixedTarget != 0);
        super.setFixedTarget(fixedTarget);
    }

}
