package uk.ac.ox.oxfish.model.regs.policymakers.factory;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.regs.policymakers.CloseReopenOnEffortDecorator;
import uk.ac.ox.oxfish.model.regs.policymakers.IndexTargetController;
import uk.ac.ox.oxfish.model.regs.policymakers.LBSPREffortPolicyFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.FixedTargetAsMultipleOfOriginalObservation;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.PastAverageSensor;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * The ITE5/ITE10 kind of controller from the DLMtoolkit.
 * Fundamentally increases/decreases effort as a function of the ratio between current indicator and target indicator
 * where target indicator is the initial 5 year average indicator times a multiplier
 */
public class ITEControllerFactory implements AlgorithmFactory<AdditionalStartable> {


    private String indicatorColumnName = "Average Trip Income";

    private DoubleParameter multiplier = new FixedDoubleParameter(1.0);

    //0.05 is ITE5; 0.1 is ITE10
    private DoubleParameter maxChangePerYear = new FixedDoubleParameter(.1);

    /**
     * what kind of effort are we actually modifying!
     * Keys are in the EFFORT_ACTUATOR map
     */
    private String effortDefinition = "season";

    /**
     * when effort is below 1 we will also close down the fishery to new entrants?
     */
    private boolean blockEntryWhenSeasonIsNotFull = true;

    /**
     * how many years after the start is called we should activate the indicator?
     */
    private int yearsBeforeStarting = 10;

    private DoubleParameter yearsToLookBackForTarget = new FixedDoubleParameter(5);

    @Override
    public AdditionalStartable apply(FishState fishState) {
        Preconditions.checkArgument(LBSPREffortPolicyFactory.EFFORT_ACTUATORS.containsKey(effortDefinition),
                "The valid effort actuators are " + LBSPREffortPolicyFactory.EFFORT_ACTUATORS.keySet());

        final Actuator<FishState, Double> effortActuator = LBSPREffortPolicyFactory.EFFORT_ACTUATORS.get(effortDefinition);


        return new AdditionalStartable() {
            @Override
            public void start(FishState model) {
                if(yearsBeforeStarting<=0)
                    starterMethod(model,effortActuator).step(model);
                else
                    fishState.scheduleOnceInXDays(
                            starterMethod(model, effortActuator),
                            StepOrder.DAWN,
                            365 * yearsBeforeStarting + 1

                    );
            }
        };
    }

    @NotNull
    private Steppable starterMethod(FishState model, Actuator<FishState, Double> effortActuator) {
        return new Steppable() {
            @Override
            public void step(SimState simState) {
                IndexTargetController controller =
                        new IndexTargetController(
                                new PastAverageSensor(
                                        indicatorColumnName,
                                        1
                                ),
                                new FixedTargetAsMultipleOfOriginalObservation(
                                        indicatorColumnName,
                                        multiplier.apply(model.getRandom()),
                                        yearsToLookBackForTarget.apply(model.getRandom()).intValue()
                                ),
                                blockEntryWhenSeasonIsNotFull ? new CloseReopenOnEffortDecorator(effortActuator) :
                                        effortActuator, 365,
                                maxChangePerYear.apply(model.getRandom()),
                                false,


                                false);

                controller.start(model);
                controller.step(model);
                model.getYearlyDataSet().registerGatherer("Index Ratio",
                        new Gatherer<FishState>() {
                            @Override
                            public Double apply(FishState fishState) {
                                return controller.getLastPolicy();
                            }
                        }
                        , Double.NaN);

            }
        };
    }


    public String getIndicatorColumnName() {
        return indicatorColumnName;
    }

    public void setIndicatorColumnName(String indicatorColumnName) {
        this.indicatorColumnName = indicatorColumnName;
    }

    public DoubleParameter getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(DoubleParameter multiplier) {
        this.multiplier = multiplier;
    }

    public DoubleParameter getMaxChangePerYear() {
        return maxChangePerYear;
    }

    public void setMaxChangePerYear(DoubleParameter maxChangePerYear) {
        this.maxChangePerYear = maxChangePerYear;
    }

    public String getEffortDefinition() {
        return effortDefinition;
    }

    public void setEffortDefinition(String effortDefinition) {
        this.effortDefinition = effortDefinition;
    }

    public boolean isBlockEntryWhenSeasonIsNotFull() {
        return blockEntryWhenSeasonIsNotFull;
    }

    public void setBlockEntryWhenSeasonIsNotFull(boolean blockEntryWhenSeasonIsNotFull) {
        this.blockEntryWhenSeasonIsNotFull = blockEntryWhenSeasonIsNotFull;
    }

    public int getYearsBeforeStarting() {
        return yearsBeforeStarting;
    }

    public void setYearsBeforeStarting(int yearsBeforeStarting) {
        this.yearsBeforeStarting = yearsBeforeStarting;
    }

    public DoubleParameter getYearsToLookBackForTarget() {
        return yearsToLookBackForTarget;
    }

    public void setYearsToLookBackForTarget(DoubleParameter yearsToLookBackForTarget) {
        this.yearsToLookBackForTarget = yearsToLookBackForTarget;
    }
}
