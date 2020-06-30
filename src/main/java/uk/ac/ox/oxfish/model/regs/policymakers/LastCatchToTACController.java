package uk.ac.ox.oxfish.model.regs.policymakers;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.FixedTargetAsMultipleOfOriginalObservation;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.ISlope;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * reads last year catches, multiplies it and uses as TAC forever
 */
public class LastCatchToTACController implements AlgorithmFactory<AdditionalStartable> {

    private DoubleParameter catchesToTargetMultiplier = new FixedDoubleParameter(1);


    private String catchColumnName = "Species 0 Landings";

    private int startingYear = 10;


    @Override
    public AdditionalStartable apply(FishState fishState) {

        return new AdditionalStartable() {
            @Override
            public void start(FishState model) {
                fishState.scheduleOnceInXDays(
                        new Steppable() {
                            @Override
                            public void step(SimState simState) {

                                TargetToTACController controller = new TargetToTACController(
                                        new FixedTargetAsMultipleOfOriginalObservation(
                                                catchColumnName,
                                                catchesToTargetMultiplier.apply(fishState.getRandom()),
                                                1
                                        ),
                                        365
                                );
                                controller.start(model);
                                controller.step(model);
                            }
                        },
                        StepOrder.DAWN,
                        365*startingYear+1);



            }
        };

    }

    public DoubleParameter getCatchesToTargetMultiplier() {
        return catchesToTargetMultiplier;
    }

    public void setCatchesToTargetMultiplier(DoubleParameter catchesToTargetMultiplier) {
        this.catchesToTargetMultiplier = catchesToTargetMultiplier;
    }

    public String getCatchColumnName() {
        return catchColumnName;
    }

    public void setCatchColumnName(String catchColumnName) {
        this.catchColumnName = catchColumnName;
    }

    public int getStartingYear() {
        return startingYear;
    }

    public void setStartingYear(int startingYear) {
        this.startingYear = startingYear;
    }
}
