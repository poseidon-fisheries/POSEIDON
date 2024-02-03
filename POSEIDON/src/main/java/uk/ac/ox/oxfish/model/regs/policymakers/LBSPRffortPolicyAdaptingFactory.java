package uk.ac.ox.oxfish.model.regs.policymakers;

import com.google.common.base.Preconditions;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.boxcars.SPRAgent;
import uk.ac.ox.oxfish.biology.boxcars.SPRAgentBuilder;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.plugins.CatchAtLengthFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import static uk.ac.ox.oxfish.model.regs.policymakers.LBSPREffortPolicyFactory.EFFORT_ACTUATORS;

/**
 * like LBSPREffortPolicyFactory with an internal SPR agent which gets its M/K adjusted over time
 */
public class LBSPRffortPolicyAdaptingFactory implements AlgorithmFactory<AdditionalStartable> {

    private CatchAtLengthFactory sprAgentDelegate = new SPRAgentBuilder();


    private LBSPREffortPolicyFactory controllerDelegate = new LBSPREffortPolicyFactory();


    private DoubleParameter upperDiscrepancyThreshold = new FixedDoubleParameter(-0.24);

    private DoubleParameter lowerDiscrepancyThreshold = new FixedDoubleParameter(-0.36);

    private DoubleParameter lowestMKAllowed = new FixedDoubleParameter(0.4);

    private DoubleParameter highestMKAllowed = new FixedDoubleParameter(2);

    private DoubleParameter cpueHalfPeriod = new FixedDoubleParameter(3);

    private DoubleParameter startUpdatingMKAfterYear = new FixedDoubleParameter(-1);


    @Override
    public AdditionalStartable apply(FishState fishState) {


        Preconditions.checkArgument(
            EFFORT_ACTUATORS.containsKey(getEffortDefinition()),
            "The valid effort actuators are " + EFFORT_ACTUATORS.keySet()
        );

        //create SPR agent
        final SPRAgent sprAgent = sprAgentDelegate.apply(fishState);


        return model -> {
            sprAgent.start(model);

            fishState.scheduleOnceInXDays(
                (Steppable) simState -> {
                    final FishState model1 = (FishState) simState;
                    LBSPRPolicyUpdater lbspr = new LBSPRPolicyUpdater(
                        sprAgent,
                        new LBSPREffortPolicy(
                            "SPR " +
                                sprAgentDelegate.getSpeciesName() + " " +
                                sprAgentDelegate.getSurveyTag(),
                            getLinearParameter().applyAsDouble(fishState.getRandom()),
                            getCubicParameter().applyAsDouble(fishState.getRandom()),
                            getSprTarget().applyAsDouble(fishState.getRandom()),
                            getMaxChangeEachYear().applyAsDouble(fishState.getRandom()),
                            EFFORT_ACTUATORS.get(getEffortDefinition()),
                            isBlockEntryWhenSeasonIsNotFull()
                        ),
                        upperDiscrepancyThreshold.applyAsDouble(model1.getRandom()),
                        lowerDiscrepancyThreshold.applyAsDouble(model1.getRandom()),
                        (int) cpueHalfPeriod.applyAsDouble(model1.getRandom()),
                        lowestMKAllowed.applyAsDouble(model1.getRandom()),
                        highestMKAllowed.applyAsDouble(model1.getRandom()),


                        (int) startUpdatingMKAfterYear.applyAsDouble(model1.getRandom())
                    );
                    lbspr.start(model1);
                    lbspr.step(model1);


                    //creaqte also a collector
                    fishState.getYearlyDataSet().registerGatherer(
                        "LBSPREffortPolicy output",
                        (Gatherer<FishState>) fishState1 -> lbspr.getAccumulatedDelta(),
                        Double.NaN
                    );
                },
                StepOrder.DAWN,
                365 * getStartingYear() + 1
            );
        };
    }

    public String getEffortDefinition() {
        return controllerDelegate.getEffortDefinition();
    }

    public DoubleParameter getLinearParameter() {
        return controllerDelegate.getLinearParameter();
    }

    public void setLinearParameter(DoubleParameter linearParameter) {
        controllerDelegate.setLinearParameter(linearParameter);
    }

    public DoubleParameter getCubicParameter() {
        return controllerDelegate.getCubicParameter();
    }

    public void setCubicParameter(DoubleParameter cubicParameter) {
        controllerDelegate.setCubicParameter(cubicParameter);
    }

    public DoubleParameter getSprTarget() {
        return controllerDelegate.getSprTarget();
    }

    public void setSprTarget(DoubleParameter sprTarget) {
        controllerDelegate.setSprTarget(sprTarget);
    }

    public DoubleParameter getMaxChangeEachYear() {
        return controllerDelegate.getMaxChangeEachYear();
    }

    public void setMaxChangeEachYear(DoubleParameter maxChangeEachYear) {
        controllerDelegate.setMaxChangeEachYear(maxChangeEachYear);
    }

    public boolean isBlockEntryWhenSeasonIsNotFull() {
        return controllerDelegate.isBlockEntryWhenSeasonIsNotFull();
    }

    public int getStartingYear() {
        return controllerDelegate.getStartingYear();
    }

    public void setStartingYear(int startingYear) {
        controllerDelegate.setStartingYear(startingYear);
    }

    public void setBlockEntryWhenSeasonIsNotFull(boolean blockEntryWhenSeasonIsNotFull) {
        controllerDelegate.setBlockEntryWhenSeasonIsNotFull(blockEntryWhenSeasonIsNotFull);
    }

    public CatchAtLengthFactory getSprAgentDelegate() {
        return sprAgentDelegate;
    }

    public void setSprAgentDelegate(CatchAtLengthFactory sprAgentDelegate) {
        this.sprAgentDelegate = sprAgentDelegate;
    }

    public DoubleParameter getUpperDiscrepancyThreshold() {
        return upperDiscrepancyThreshold;
    }

    public void setUpperDiscrepancyThreshold(DoubleParameter upperDiscrepancyThreshold) {
        this.upperDiscrepancyThreshold = upperDiscrepancyThreshold;
    }

    public DoubleParameter getLowerDiscrepancyThreshold() {
        return lowerDiscrepancyThreshold;
    }

    public void setLowerDiscrepancyThreshold(DoubleParameter lowerDiscrepancyThreshold) {
        this.lowerDiscrepancyThreshold = lowerDiscrepancyThreshold;
    }

    public DoubleParameter getLowestMKAllowed() {
        return lowestMKAllowed;
    }

    public void setLowestMKAllowed(DoubleParameter lowestMKAllowed) {
        this.lowestMKAllowed = lowestMKAllowed;
    }

    public DoubleParameter getHighestMKAllowed() {
        return highestMKAllowed;
    }

    public void setHighestMKAllowed(DoubleParameter highestMKAllowed) {
        this.highestMKAllowed = highestMKAllowed;
    }

    public DoubleParameter getCpueHalfPeriod() {
        return cpueHalfPeriod;
    }

    public void setCpueHalfPeriod(DoubleParameter cpueHalfPeriod) {
        this.cpueHalfPeriod = cpueHalfPeriod;
    }

    public DoubleParameter getStartUpdatingMKAfterYear() {
        return startUpdatingMKAfterYear;
    }

    public void setStartUpdatingMKAfterYear(DoubleParameter startUpdatingMKAfterYear) {
        this.startUpdatingMKAfterYear = startUpdatingMKAfterYear;
    }
}
