package uk.ac.ox.oxfish.model.regs.policymakers;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.boxcars.SPRAgent;
import uk.ac.ox.oxfish.biology.boxcars.SPRAgentBuilder;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.plugins.CatchAtLengthFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static uk.ac.ox.oxfish.model.regs.policymakers.LBSPREffortPolicyFactory.EFFORT_ACTUATORS;

/**
 * like LBSPREffortPolicyFactory with an internal SPR agent which gets its M/K adjusted over time
 */
public class LBSPRffortPolicyAdaptingFactory implements AlgorithmFactory<AdditionalStartable> {

    private CatchAtLengthFactory sprAgentDelegate = new SPRAgentBuilder();


    private LBSPREffortPolicyFactory controllerDelegate = new LBSPREffortPolicyFactory();


    private DoubleParameter upperDiscrepancyThreshold = new FixedDoubleParameter(-0.24);

    private DoubleParameter lowerDiscrepancyThreshold = new FixedDoubleParameter(-0.36);

    private DoubleParameter lowestMKAllowed =new FixedDoubleParameter(0.4);

    private DoubleParameter highestMKAllowed =new FixedDoubleParameter(2);

    private DoubleParameter cpueHalfPeriod  =new FixedDoubleParameter(3);

    private DoubleParameter startUpdatingMKAfterYear = new FixedDoubleParameter(-1);


    @Override
    public AdditionalStartable apply(FishState fishState) {


        Preconditions.checkArgument(EFFORT_ACTUATORS.containsKey(getEffortDefinition()),
                "The valid effort actuators are " + EFFORT_ACTUATORS.keySet());

        //create SPR agent
        final SPRAgent sprAgent = sprAgentDelegate.apply(fishState);


        return new AdditionalStartable() {
            @Override
            public void start(FishState model) {
                sprAgent.start(model);

                fishState.scheduleOnceInXDays(
                        new Steppable() {
                            @Override
                            public void step(SimState simState) {
                                final FishState model = (FishState) simState;
                                LBSPRPolicyUpdater lbspr = new LBSPRPolicyUpdater(
                                        sprAgent,
                                        new LBSPREffortPolicy(
                                                "SPR " +
                                                        sprAgentDelegate.getSpeciesName() + " " +
                                                        sprAgentDelegate.getSurveyTag(),
                                                getLinearParameter().apply(fishState.getRandom()),
                                                getCubicParameter().apply(fishState.getRandom()),
                                                getSprTarget().apply(fishState.getRandom()),
                                                getMaxChangeEachYear().apply(fishState.getRandom()),
                                                EFFORT_ACTUATORS.get(getEffortDefinition()),
                                                isBlockEntryWhenSeasonIsNotFull()),
                                        upperDiscrepancyThreshold.apply(model.getRandom()),
                                        lowerDiscrepancyThreshold.apply(model.getRandom()),
                                        cpueHalfPeriod.apply(model.getRandom()).intValue(),
                                        lowestMKAllowed.apply(model.getRandom()),
                                        highestMKAllowed.apply(model.getRandom()),


                                        startUpdatingMKAfterYear.apply(model.getRandom()).intValue());
                                lbspr.start(model);
                                lbspr.step(model);


                                //creaqte also a collector
                                fishState.getYearlyDataSet().registerGatherer(
                                        "LBSPREffortPolicy output",
                                        new Gatherer<FishState>() {
                                            @Override
                                            public Double apply(FishState fishState) {
                                                return lbspr.getAccumulatedDelta();
                                            }
                                        },
                                        Double.NaN
                                );
                            }
                        },
                        StepOrder.DAWN,
                        365 * getStartingYear() + 1
                );
            }
        };
    }


    public CatchAtLengthFactory getSprAgentDelegate() {
        return sprAgentDelegate;
    }

    public void setSprAgentDelegate(CatchAtLengthFactory sprAgentDelegate) {
        this.sprAgentDelegate = sprAgentDelegate;
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

    public String getEffortDefinition() {
        return controllerDelegate.getEffortDefinition();
    }

    public int getStartingYear() {
        return controllerDelegate.getStartingYear();
    }

    public void setStartingYear(int startingYear) {
        controllerDelegate.setStartingYear(startingYear);
    }

    public boolean isBlockEntryWhenSeasonIsNotFull() {
        return controllerDelegate.isBlockEntryWhenSeasonIsNotFull();
    }

    public void setBlockEntryWhenSeasonIsNotFull(boolean blockEntryWhenSeasonIsNotFull) {
        controllerDelegate.setBlockEntryWhenSeasonIsNotFull(blockEntryWhenSeasonIsNotFull);
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
