package uk.ac.ox.oxfish.model.regs.policymakers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.UnchangingPastSensor;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.PastAverageSensor;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;

public class LTargetEffortPolicy extends Controller {

    final private double proportionAverageToTarget;


    private double suggestedEffort = 1;

    public LTargetEffortPolicy(String meanLengthColumnName,
                               double proportionAverageToTarget,
                               int periodTimeInYears,
                               Actuator<FishState,Double> effortActuator,
                               boolean closeEntryWhenNeeded){

        super(
                new PastAverageSensor(meanLengthColumnName,periodTimeInYears),
                new UnchangingPastSensor(meanLengthColumnName,1.0,
                        periodTimeInYears*2),
                closeEntryWhenNeeded ? new CloseReopenOnEffortDecorator(effortActuator):
                        effortActuator,
                periodTimeInYears*365

        );

        this.proportionAverageToTarget = proportionAverageToTarget;


    }

    @Override
    public double computePolicy(double recentAverageLength, double historicalAverageLength,
                                FishState model,
                                double oldPolicy) {
        if(!Double.isFinite(oldPolicy))
            oldPolicy = 1d;

        suggestedEffort = oldPolicy * computePolicyMultiplier(recentAverageLength,
                historicalAverageLength,
                proportionAverageToTarget, 0.9);
        return suggestedEffort;
    }


    public static double computePolicyMultiplier(double recentIndex,
                                                 double historicalIndex,
                                                 double proportionAverageToTarget,
                                                 double proportionAverageToIndexZero){

        double indexZero = historicalIndex * proportionAverageToIndexZero;
        final double lengthTarget = historicalIndex * proportionAverageToTarget;

        if(recentIndex < indexZero)
            return 0.5 * Math.pow(recentIndex/indexZero,2);
        else{
            //0.5 *   (1 + ((Lrecent - L0)/(Ltarget - L0)))
            double numerator = recentIndex - indexZero;
            double denominator = lengthTarget - indexZero;
            return 0.5 * (1 + (numerator/denominator));
        }






    }


    public double getProportionAverageToTarget() {
        return proportionAverageToTarget;
    }

    public double getSuggestedEffort() {
        return suggestedEffort;
    }
}
