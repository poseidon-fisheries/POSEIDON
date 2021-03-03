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

import java.util.Map;

public class LoptEffortPolicyFactory implements AlgorithmFactory<AdditionalStartable> {



    private String meanLengthColumnName = "Mean Length Caught " + "Lutjanus malabaricus" + " " + "spr_agent_total";

    private DoubleParameter targetLength = new FixedDoubleParameter(60);

    private DoubleParameter bufferValue = new FixedDoubleParameter(0.9);

    private DoubleParameter howManyYearsToLookBackTo = new FixedDoubleParameter(5);

    private String effortDefinition = "season";


    private boolean blockEntryWhenSeasonIsNotFull = false;

    private int startingYear = 7;

    @Override
    public AdditionalStartable apply(FishState fishState) {


        final Map<String, Actuator<FishState, Double>> effortActuators = LBSPREffortPolicyFactory.EFFORT_ACTUATORS;
        Preconditions.checkArgument(effortActuators.containsKey(effortDefinition),
                "The valid effort actuators are " + effortActuators.keySet());



        return new AdditionalStartable() {
            @Override
            public void start(FishState model) {
                fishState.scheduleOnceInXDays(
                        new Steppable() {
                            @Override
                            public void step(SimState simState) {
                                LoptEffortPolicy lopt = new LoptEffortPolicy(
                                        meanLengthColumnName,
                                        bufferValue.apply(fishState.getRandom()),
                                        targetLength.apply(fishState.getRandom()),
                                        howManyYearsToLookBackTo.apply(fishState.getRandom()).intValue(),
                                        effortActuators.get(effortDefinition),
                                        blockEntryWhenSeasonIsNotFull);
                                lopt.start(model);
                                lopt.step(model);


                                //creaqte also a collector
                                fishState.getYearlyDataSet().registerGatherer(
                                        "LoptEffortPolicy output",
                                        new Gatherer<FishState>() {
                                            @Override
                                            public Double apply(FishState fishState) {
                                                return lopt.getTheoreticalSuggestedEffort();
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


    public String getMeanLengthColumnName() {
        return meanLengthColumnName;
    }

    public void setMeanLengthColumnName(String meanLengthColumnName) {
        this.meanLengthColumnName = meanLengthColumnName;
    }

    public DoubleParameter getTargetLength() {
        return targetLength;
    }

    public void setTargetLength(DoubleParameter targetLength) {
        this.targetLength = targetLength;
    }

    public DoubleParameter getBufferValue() {
        return bufferValue;
    }

    public void setBufferValue(DoubleParameter bufferValue) {
        this.bufferValue = bufferValue;
    }

    public DoubleParameter getHowManyYearsToLookBackTo() {
        return howManyYearsToLookBackTo;
    }

    public void setHowManyYearsToLookBackTo(DoubleParameter howManyYearsToLookBackTo) {
        this.howManyYearsToLookBackTo = howManyYearsToLookBackTo;
    }

    public String getEffortDefinition() {
        return effortDefinition;
    }

    public boolean isBlockEntryWhenSeasonIsNotFull() {
        return blockEntryWhenSeasonIsNotFull;
    }

    public void setBlockEntryWhenSeasonIsNotFull(boolean blockEntryWhenSeasonIsNotFull) {
        this.blockEntryWhenSeasonIsNotFull = blockEntryWhenSeasonIsNotFull;
    }

    public int getStartingYear() {
        return startingYear;
    }

    public void setStartingYear(int startingYear) {
        this.startingYear = startingYear;
    }

    public void setEffortDefinition(String effortDefinition) {
        this.effortDefinition = effortDefinition;
    }
}
