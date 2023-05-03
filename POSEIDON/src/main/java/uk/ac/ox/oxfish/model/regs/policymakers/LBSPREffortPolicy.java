package uk.ac.ox.oxfish.model.regs.policymakers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.EntryPlugin;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

/**
 * multiplies previous effort by a function of the proportion of current SPR to target
 */
public class LBSPREffortPolicy extends Controller {

    private final String columnNameSPR;

    private final double linearParameter;

    private final double cubicParameter;

    private final double sprTarget; //usually this is 30/40%

    private final double maxChangeInPercentage;

    /**
     * I am assuming the actuator itself just wants the final "effort" multiplier;
     * here we store 1 * delta(t=1) * delta(t=2) * ... * delta(now)
     */
    private double accumulatedDelta = 1;

    private final boolean blockEntryWhenSeasonIsNotFull;


    public LBSPREffortPolicy(String columnNameSPR, double linearParameter,
                             double cubicParameter, double sprTarget,
                             double maxChangeInPercentage,
                             Actuator<FishState, Double> effortActuator, boolean blockEntryWhenSeasonIsNotFull) {

        super(
                (Sensor<FishState, Double>) system ->
                        system.getLatestYearlyObservation(columnNameSPR),
                (Sensor<FishState, Double>) system ->
                        sprTarget,
                effortActuator,
                365
        );
        this.columnNameSPR = columnNameSPR;
        this.linearParameter = linearParameter;
        this.cubicParameter = cubicParameter;
        this.sprTarget = sprTarget;
        this.maxChangeInPercentage = maxChangeInPercentage;
        this.blockEntryWhenSeasonIsNotFull = blockEntryWhenSeasonIsNotFull;
    }

    /**
     * this is the formula in the DLM toolkit as of Nov 10, 2020
     */
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


    @Override
    public double computePolicy(double currentSPR,
                                double targetSPR,
                                FishState model,
                                double oldPolicy) {
        //numerical errors can happen; don't act then
        if(!Double.isFinite(currentSPR) || currentSPR < 0 || currentSPR > 1)
            return accumulatedDelta;

        double deltaToday =
                lbsprPolicyEffortProportion(linearParameter,cubicParameter,currentSPR,targetSPR);
        if(deltaToday<-maxChangeInPercentage)
                deltaToday = -maxChangeInPercentage;
        if(deltaToday>maxChangeInPercentage)
            deltaToday = maxChangeInPercentage;


        System.out.println("effort is now " + accumulatedDelta);
        accumulatedDelta = accumulatedDelta * (1+deltaToday);

        if(accumulatedDelta>=1 && blockEntryWhenSeasonIsNotFull)
            for (EntryPlugin entryPlugin : model.getEntryPlugins()) {
                entryPlugin.setEntryPaused(false);
            }
        if(accumulatedDelta<1 && blockEntryWhenSeasonIsNotFull)
            for (EntryPlugin entryPlugin : model.getEntryPlugins()) {
                entryPlugin.setEntryPaused(true);
            }


        return Math.min(accumulatedDelta,1);
    }

    /**
     * This is the formula in the DLM toolkit in 2019; it was a simpler bang-bang controller
     */
    public static double lbsprPolicyEffortBangBang(
            final double currentSPR,
            final double targetSPR
    ){

        double ratio = currentSPR/targetSPR;
        if(ratio > 1.25)
            return 1.1;
        if(ratio < 0.75)
            return 0.9;
        else
            return 1.0;
    }

    public double getAccumulatedDelta() {
        return accumulatedDelta;
    }

    public String getColumnNameSPR() {
        return columnNameSPR;
    }

    public double getLinearParameter() {
        return linearParameter;
    }

    public double getCubicParameter() {
        return cubicParameter;
    }

    public double getSprTarget() {
        return sprTarget;
    }
}
