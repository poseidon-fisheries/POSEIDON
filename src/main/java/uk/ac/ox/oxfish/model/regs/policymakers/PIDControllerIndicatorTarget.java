package uk.ac.ox.oxfish.model.regs.policymakers;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.FixedTargetAsMultipleOfOriginalObservation;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.PastAverageSensor;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class PIDControllerIndicatorTarget implements AlgorithmFactory<AdditionalStartable> {



    private String indicatorColumnName = "Average Trip Duration";

    private DoubleParameter indicatorMultiplier = new FixedDoubleParameter(0.8);

    private int numberOfYearsToLookBackForTarget = 10;

    private int numberOfYearsToAverageForCurrent = 1;

    private String offsetColumnName = "Species 0 Landings";

    private int numberOfYearsToAverageForOffset = 1;

    private DoubleParameter averageToOffsetMultiplier = new FixedDoubleParameter(0.8);

    private int startingYear = 10;

    private boolean overflowAtZeroProtection = true;

    private boolean negative = false;

    private boolean integrated = false;

    private double minimumTAC = 0;

    @Override
    public AdditionalStartable apply(FishState fishState) {

        FixedTargetAsMultipleOfOriginalObservation target = new
                FixedTargetAsMultipleOfOriginalObservation(indicatorColumnName,
                indicatorMultiplier.apply(fishState.getRandom()),
                numberOfYearsToLookBackForTarget);


        PastAverageSensor current = new PastAverageSensor(
                indicatorColumnName,
                numberOfYearsToAverageForCurrent
        );

        FixedTargetAsMultipleOfOriginalObservation offset = new
                FixedTargetAsMultipleOfOriginalObservation(offsetColumnName,
                averageToOffsetMultiplier.apply(fishState.getRandom()),
                numberOfYearsToAverageForOffset);


        PIDController controller = new PIDController(
                current,
                target,
                TargetToTACController.POLICY_TO_TAC_ACTUATOR,
                365,
                0,
                0,
                0,
                0
        );
        controller.setOffsetSetter(offset);
        controller.setZeroOverflowProtection(overflowAtZeroProtection);
        controller.setMinimumPolicy(minimumTAC);

        return model -> fishState.scheduleOnceInXDays(
                new Steppable() {
                    @Override
                    public void step(SimState simState) {
//                        controller.setP(
//                                - 0.1 *
//                        );
                        final Double meanIndicator = new PastAverageSensor(
                                indicatorColumnName,
                                numberOfYearsToLookBackForTarget
                        ).scan(((FishState) simState));
                        System.out.println("mean_index:" +
                                meanIndicator);
                        final Double meanOffset = new PastAverageSensor(
                                offsetColumnName,
                                numberOfYearsToLookBackForTarget
                        ).scan(((FishState) simState));
                        System.out.println("mean_offset:" +
                                meanOffset);

                        if(!integrated) {
                            controller.setP(0.5 * (meanOffset / meanIndicator));
                        }
                        else{
                            controller.setP(0.5 * (meanOffset / meanIndicator));
                            controller.setI(0.05 * (meanOffset / meanIndicator));

                        }
                        if(negative) {
                            controller.setP(-controller.getP());
                            controller.setI(-controller.getI());

                        }

                        System.out.println("p controller: " + controller.getP());
                        controller.start(model);
                        controller.step(model);
                    }
                },
                StepOrder.DAWN,
                365 * startingYear + 1
        );


    }

    public String getIndicatorColumnName() {
        return indicatorColumnName;
    }

    public void setIndicatorColumnName(String indicatorColumnName) {
        this.indicatorColumnName = indicatorColumnName;
    }

    public DoubleParameter getIndicatorMultiplier() {
        return indicatorMultiplier;
    }

    public void setIndicatorMultiplier(DoubleParameter indicatorMultiplier) {
        this.indicatorMultiplier = indicatorMultiplier;
    }

    public int getNumberOfYearsToLookBackForTarget() {
        return numberOfYearsToLookBackForTarget;
    }

    public void setNumberOfYearsToLookBackForTarget(int numberOfYearsToLookBackForTarget) {
        this.numberOfYearsToLookBackForTarget = numberOfYearsToLookBackForTarget;
    }

    public int getNumberOfYearsToAverageForCurrent() {
        return numberOfYearsToAverageForCurrent;
    }

    public void setNumberOfYearsToAverageForCurrent(int numberOfYearsToAverageForCurrent) {
        this.numberOfYearsToAverageForCurrent = numberOfYearsToAverageForCurrent;
    }

    public String getOffsetColumnName() {
        return offsetColumnName;
    }

    public void setOffsetColumnName(String offsetColumnName) {
        this.offsetColumnName = offsetColumnName;
    }

    public int getNumberOfYearsToAverageForOffset() {
        return numberOfYearsToAverageForOffset;
    }

    public void setNumberOfYearsToAverageForOffset(int numberOfYearsToAverageForOffset) {
        this.numberOfYearsToAverageForOffset = numberOfYearsToAverageForOffset;
    }

    public DoubleParameter getAverageToOffsetMultiplier() {
        return averageToOffsetMultiplier;
    }

    public void setAverageToOffsetMultiplier(DoubleParameter averageToOffsetMultiplier) {
        this.averageToOffsetMultiplier = averageToOffsetMultiplier;
    }




    public int getStartingYear() {
        return startingYear;
    }

    public void setStartingYear(int startingYear) {
        this.startingYear = startingYear;
    }

    public boolean isOverflowAtZeroProtection() {
        return overflowAtZeroProtection;
    }

    public void setOverflowAtZeroProtection(boolean overflowAtZeroProtection) {
        this.overflowAtZeroProtection = overflowAtZeroProtection;
    }

    public boolean isNegative() {
        return negative;
    }

    public void setNegative(boolean negative) {
        this.negative = negative;
    }

    public double getMinimumTAC() {
        return minimumTAC;
    }

    public void setMinimumTAC(double minimumTAC) {
        this.minimumTAC = minimumTAC;
    }

    public boolean isIntegrated() {
        return integrated;
    }

    public void setIntegrated(boolean integrated) {
        this.integrated = integrated;
    }
}
