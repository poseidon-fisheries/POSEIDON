package uk.ac.ox.oxfish.model.regs.policymakers.factory;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.ISlope;
import uk.ac.ox.oxfish.model.regs.policymakers.TargetToTACController;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class ISlopeToTACControllerFactory implements AlgorithmFactory<AdditionalStartable> {


    private String indicatorColumnName = "Species 0 CPUE";
    private String catchColumnName = "Species 0 Landings";

    /**
     * does the quota affect all species, or is it specific to one? (notice that when that species quota is over, the whole fishery closes)
     */
    private String targetedSpecies = "";

    private DoubleParameter gainLambdaParameter = new FixedDoubleParameter(0.4);

    private DoubleParameter precautionaryScaling = new FixedDoubleParameter(0.8);

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
                                if(targetedSpecies.trim().isEmpty())
                                    controller = new TargetToTACController(
                                            new ISlope(
                                                    catchColumnName,
                                                    indicatorColumnName,
                                                    gainLambdaParameter.apply(model.getRandom()),
                                                    precautionaryScaling.apply(model.getRandom()),
                                                    interval
                                            )
                                    );
                                else
                                    controller = new TargetToTACController(
                                            new ISlope(
                                                    catchColumnName,
                                                    indicatorColumnName,
                                                    gainLambdaParameter.apply(model.getRandom()),
                                                    precautionaryScaling.apply(model.getRandom()),
                                                    interval
                                            ),
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

    public DoubleParameter getGainLambdaParameter() {
        return gainLambdaParameter;
    }

    public void setGainLambdaParameter(DoubleParameter gainLambdaParameter) {
        this.gainLambdaParameter = gainLambdaParameter;
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
}




