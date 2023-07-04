package uk.ac.ox.oxfish.model.regs.policymakers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.PastAverageSensor;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.UnchangingPastSensor;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;

public class LTargetEffortPolicy extends Controller {

    private static final long serialVersionUID = -3365800714403551902L;
    final private double proportionAverageToTarget;


    private double suggestedEffort = 1;

    public LTargetEffortPolicy(
        final String meanLengthColumnName,
        final double proportionAverageToTarget,
        final int yearsBackToAverage,
        final Actuator<FishState, Double> effortActuator,
        final boolean closeEntryWhenNeeded, final int updateEffortPeriodInYears
    ) {

        super(
            new PastAverageSensor(meanLengthColumnName, yearsBackToAverage),
            new UnchangingPastSensor(meanLengthColumnName, 1.0,
                yearsBackToAverage * 2
            ),
            closeEntryWhenNeeded ? new CloseReopenOnEffortDecorator(effortActuator) :
                effortActuator,
            updateEffortPeriodInYears * 365

        );

        this.proportionAverageToTarget = proportionAverageToTarget;


    }

    @Override
    public double computePolicy(
        final double recentAverageLength, final double historicalAverageLength,
        final FishState model,
        double oldPolicy
    ) {
        if (!Double.isFinite(oldPolicy))
            oldPolicy = 1d;

        suggestedEffort = oldPolicy * computePolicyMultiplier(recentAverageLength,
            historicalAverageLength,
            proportionAverageToTarget, 0.9
        );
        return suggestedEffort;
    }


    public static double computePolicyMultiplier(
        final double recentIndex,
        final double historicalIndex,
        final double proportionAverageToTarget,
        final double proportionAverageToIndexZero
    ) {

        final double indexZero = historicalIndex * proportionAverageToIndexZero;
        final double lengthTarget = historicalIndex * proportionAverageToTarget;

        if (recentIndex < indexZero)
            return 0.5 * Math.pow(recentIndex / indexZero, 2);
        else {
            //0.5 *   (1 + ((Lrecent - L0)/(Ltarget - L0)))
            final double numerator = recentIndex - indexZero;
            final double denominator = lengthTarget - indexZero;
            return 0.5 * (1 + (numerator / denominator));
        }


    }


    public double getProportionAverageToTarget() {
        return proportionAverageToTarget;
    }

    public double getSuggestedEffort() {
        return suggestedEffort;
    }
}
