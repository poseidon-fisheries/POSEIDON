package uk.ac.ox.oxfish.model.regs.policymakers;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.boxcars.SPRAgent;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.ITarget;

/**
 * changes M/K in SPR computation to avoid large discrepancies between what the SPR
 * rule says and what ITarget would say.
 */
public class LBSPRPolicyUpdater implements Steppable, AdditionalStartable {


    /**
     * keep the SPR internal so we can change its parameters on the spot
     */
    private final SPRAgent internalSPRAgent;

    /**
     * the object actually making decisions  and policies
     */
    private final LBSPREffortPolicy controller;


    /**
     * the object used to transform CPUE into effort (used to adjust M/K, not directly into policy)
     */
    private final ITarget cpueToEffort;


    private final double upperDiscrepancyThreshold;

    private final double lowerDiscrepancyThreshold;

    private final double minimumMK;

    private final double maximumMK;

    /**
     * do not update M/K before this year
     */
    private final int startUpdatingAfterYear;

    public LBSPRPolicyUpdater(SPRAgent internalSPRAgent, LBSPREffortPolicy controller,
                              double upperDiscrepancyThreshold, double lowerDiscrepancyThreshold,
                              int cpueHalfPeriod, double minimumMK, double maximumMK, int startUpdatingAfterYear) {
        this.internalSPRAgent = internalSPRAgent;
        this.controller = controller;
        this.upperDiscrepancyThreshold = upperDiscrepancyThreshold;
        this.lowerDiscrepancyThreshold = lowerDiscrepancyThreshold;
        this.minimumMK = minimumMK;
        this.maximumMK = maximumMK;
        this.startUpdatingAfterYear = startUpdatingAfterYear;

        cpueToEffort = new ITarget("not used",
                "CPUE " + internalSPRAgent.getSpecies() + " " + internalSPRAgent.getSurveyTag(),
                1.0,
                1.5,
                cpueHalfPeriod,
                -1 //not used!
        );
    }

    /**
     * here we update the SPR M/K
     * @param simState
     */
    @Override
    public void step(SimState simState) {

        final FishState model = (FishState) simState;

        //first apply the controller as usual
        controller.step(simState);


        //if there are not enough observations, don't bother updating
        if(   model.getYear()< startUpdatingAfterYear ||
                model.getYearlyDataSet().getColumn(cpueToEffort.getIndicatorColumnName()).size() <
                        cpueToEffort.getTimeInterval()*2

        )
            return;

        double targetSPR = 0.4;
        double currentSPR = model.getLatestYearlyObservation("SPR " + internalSPRAgent.getSpecies() +" " + internalSPRAgent.getSurveyTag());
        double effortChangeSPR = LBSPREffortPolicy.lbsprPolicyEffortProportion(
                controller.getLinearParameter(),
                controller.getCubicParameter(),
                currentSPR,
                targetSPR
        );
        double effortChangeCPUE =cpueToEffort.getPercentageChangeToTACDueToIndicator(model);
        effortChangeCPUE = effortChangeCPUE - 1;

        double discrepancy = effortChangeCPUE - effortChangeSPR;
        if(discrepancy > upperDiscrepancyThreshold) {
            //increase M/K
            double currentMK = internalSPRAgent.getAssumedNaturalMortality()/internalSPRAgent.getAssumedKParameter();
            double newMortality =internalSPRAgent.getAssumedKParameter() * Math.min(currentMK + .1,maximumMK);
            internalSPRAgent.setAssumedNaturalMortality(newMortality);
        }
        if(discrepancy < lowerDiscrepancyThreshold){
            //decrease M/K
            double currentMK = internalSPRAgent.getAssumedNaturalMortality()/internalSPRAgent.getAssumedKParameter();
            double newMortality =internalSPRAgent.getAssumedKParameter() * Math.max(currentMK - .1,minimumMK);
            internalSPRAgent.setAssumedNaturalMortality(newMortality);
        }
        System.out.println("M/K is now " + (internalSPRAgent.getAssumedNaturalMortality()/internalSPRAgent.getAssumedKParameter()));


    }


    public double getAccumulatedDelta() {
        return controller.getAccumulatedDelta();
    }

    @Override
    public void start(FishState model) {
        //we are going to intercept the start for the controller, because we want to step it always
        //before we update
        model.scheduleEveryXDay(this, StepOrder.POLICY_UPDATE, controller.getIntervalInDays());


    }


}
