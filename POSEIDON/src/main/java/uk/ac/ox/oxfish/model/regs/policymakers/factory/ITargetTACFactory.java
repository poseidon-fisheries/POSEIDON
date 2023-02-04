package uk.ac.ox.oxfish.model.regs.policymakers.factory;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.policymakers.TargetToTACController;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.ISlope;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.ITarget;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class ITargetTACFactory  implements AlgorithmFactory<AdditionalStartable> {


    private String indicatorColumnName = "Species 0 CPUE";
    private String catchColumnName = "Species 0 Landings";

    /**
     * does the quota affect all species, or is it specific to one?
     * (notice that when that species quota is over, the whole fishery closes)
     */
    private String targetedSpecies = "";

    private DoubleParameter indicatorMultiplier = new FixedDoubleParameter(1.5);

    private DoubleParameter precautionaryScaling = new FixedDoubleParameter(0);

    private int interval = 5;
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
                                TargetToTACController controller;
                                final ITarget itarget = new ITarget(
                                        catchColumnName,
                                        indicatorColumnName,
                                        precautionaryScaling.apply(model.getRandom()),
                                        indicatorMultiplier.apply(model.getRandom()),
                                        interval,
                                        interval * 2
                                );
                                if(targetedSpecies.trim().isEmpty()) {
                                    controller = new TargetToTACController(
                                            itarget
                                    );
                                } else
                                    controller = new TargetToTACController(
                                            itarget,
                                            targetedSpecies
                                    );
                                controller.start(model);
                                controller.step(model);
                            }
                        },
                        StepOrder.DAWN,
                        365 * startingYear + 1
                );
            }
        };

    }


    public String getIndicatorColumnName() {
        return indicatorColumnName;
    }

    public void setIndicatorColumnName(String indicatorColumnName) {
        this.indicatorColumnName = indicatorColumnName;
    }

    public String getCatchColumnName() {
        return catchColumnName;
    }

    public void setCatchColumnName(String catchColumnName) {
        this.catchColumnName = catchColumnName;
    }



    public DoubleParameter getPrecautionaryScaling() {
        return precautionaryScaling;
    }

    public void setPrecautionaryScaling(DoubleParameter precautionaryScaling) {
        this.precautionaryScaling = precautionaryScaling;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getStartingYear() {
        return startingYear;
    }

    public void setStartingYear(int startingYear) {
        this.startingYear = startingYear;
    }

    public String getTargetedSpecies() {
        return targetedSpecies;
    }

    public void setTargetedSpecies(String targetedSpecies) {
        this.targetedSpecies = targetedSpecies;
    }

    public DoubleParameter getIndicatorMultiplier() {
        return indicatorMultiplier;
    }

    public void setIndicatorMultiplier(DoubleParameter indicatorMultiplier) {
        this.indicatorMultiplier = indicatorMultiplier;
    }
}

