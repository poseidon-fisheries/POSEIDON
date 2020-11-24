package uk.ac.ox.oxfish.model.regs.policymakers;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.Map;

public class LBSPREffortPolicyFactory implements AlgorithmFactory<AdditionalStartable> {


    private final static Map<String, Actuator<FishState,Double>> effortActuators = new HashMap<>();
    static {
        effortActuators.put("season",
                IndexTargetController.RATIO_TO_SEASONAL_CLOSURE
                );
        effortActuators.put("fleet",
                IndexTargetController.RATIO_TO_FLEET_SIZE
                );
        effortActuators.put("daysatsea",
                IndexTargetController.RATIO_TO_DAYSATSEA
                );
    }


    private String sprColumnName = "SPR " + "Lutjanus malabaricus" + " " + "spr_agent_total";

    private DoubleParameter linearParameter = new FixedDoubleParameter(0.05);

    private DoubleParameter cubicParameter = new FixedDoubleParameter(0.3);

    private DoubleParameter sprTarget = new FixedDoubleParameter(.4);

    private DoubleParameter maxChangeEachYear = new FixedDoubleParameter(.1);

    private final String effortDefinition = "season";


    private boolean blockEntryWhenSeasonIsNotFull = false;

    private int startingYear = 7;

    @Override
    public AdditionalStartable apply(FishState fishState) {


        Preconditions.checkArgument(effortActuators.containsKey(effortDefinition),
                "The valid effort actuators are " + effortActuators.keySet());



        return new AdditionalStartable() {
            @Override
            public void start(FishState model) {
                fishState.scheduleOnceInXDays(
                        new Steppable() {
                            @Override
                            public void step(SimState simState) {
                                LBSPREffortPolicy lbspr = new LBSPREffortPolicy(
                                        sprColumnName,
                                        linearParameter.apply(fishState.getRandom()),
                                        cubicParameter.apply(fishState.getRandom()),
                                        sprTarget.apply(fishState.getRandom()),
                                        maxChangeEachYear.apply(fishState.getRandom()),
                                        effortActuators.get(effortDefinition),
                                        blockEntryWhenSeasonIsNotFull);
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
                        365 * startingYear + 1
                );
            }
        };
    }

    public String getSprColumnName() {
        return sprColumnName;
    }

    public void setSprColumnName(String sprColumnName) {
        this.sprColumnName = sprColumnName;
    }

    public DoubleParameter getLinearParameter() {
        return linearParameter;
    }

    public void setLinearParameter(DoubleParameter linearParameter) {
        this.linearParameter = linearParameter;
    }

    public DoubleParameter getCubicParameter() {
        return cubicParameter;
    }

    public void setCubicParameter(DoubleParameter cubicParameter) {
        this.cubicParameter = cubicParameter;
    }

    public DoubleParameter getSprTarget() {
        return sprTarget;
    }

    public void setSprTarget(DoubleParameter sprTarget) {
        this.sprTarget = sprTarget;
    }

    public DoubleParameter getMaxChangeEachYear() {
        return maxChangeEachYear;
    }

    public void setMaxChangeEachYear(DoubleParameter maxChangeEachYear) {
        this.maxChangeEachYear = maxChangeEachYear;
    }

    public String getEffortDefinition() {
        return effortDefinition;
    }

    public int getStartingYear() {
        return startingYear;
    }

    public void setStartingYear(int startingYear) {
        this.startingYear = startingYear;
    }

    public boolean isBlockEntryWhenSeasonIsNotFull() {
        return blockEntryWhenSeasonIsNotFull;
    }

    public void setBlockEntryWhenSeasonIsNotFull(boolean blockEntryWhenSeasonIsNotFull) {
        this.blockEntryWhenSeasonIsNotFull = blockEntryWhenSeasonIsNotFull;
    }
}
