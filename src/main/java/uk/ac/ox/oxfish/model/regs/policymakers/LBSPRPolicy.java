package uk.ac.ox.oxfish.model.regs.policymakers;

/**
 * multiplies previous effort by a function of the proportion of current SPR to target
 */
public class LBSPRPolicy {

    private final String columnNameSPR;

    private final double linearParameter;

    private final double cubicParameter;

    private final double sprTarget; //usually this is 30/40%

    private final double maxChangeInPercentage;


    public LBSPRPolicy(String columnNameSPR, double linearParameter, double cubicParameter, double sprTarget, double maxChangeInPercentage) {
        this.columnNameSPR = columnNameSPR;
        this.linearParameter = linearParameter;
        this.cubicParameter = cubicParameter;
        this.sprTarget = sprTarget;
        this.maxChangeInPercentage = maxChangeInPercentage;
    }

    public static double lbsprPolicyEffortProportion(
            final double linearParameter,
            final double cubicParameter,
            final double currentSPR,
            final double targetSPR
    ){
        //this is in the DLM toolkit:
        //    vt <- theta1 * (ratio^3) + theta2 * ratio

        //IT IS DIFFERENT FROM THE PAPER
        return cubicParameter *
                Math.pow(currentSPR/targetSPR - 1d,3) +
                linearParameter *
                        (currentSPR/(targetSPR) - 1d);

    }

}
