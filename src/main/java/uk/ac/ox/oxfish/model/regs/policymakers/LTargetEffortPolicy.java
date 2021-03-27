package uk.ac.ox.oxfish.model.regs.policymakers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.FixedTargetAsMultipleOfOriginalObservation;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.PastAverageSensor;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.util.DoubleSummaryStatistics;

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
                new FixedTargetAsMultipleOfOriginalObservation(meanLengthColumnName,1.0,
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
                proportionAverageToTarget);
        return suggestedEffort;
    }


    public static double computePolicyMultiplier(double recentAverageLength,
                                                 double historicalAverageLength,
                                                 double proportionAverageToTarget){

        double lZero = historicalAverageLength * 0.9;
        final double lengthTarget = historicalAverageLength * proportionAverageToTarget;

        if(recentAverageLength < lZero)
            return 0.5 * Math.pow(recentAverageLength/lZero,2);
        else{
            //0.5 *   (1 + ((Lrecent - L0)/(Ltarget - L0)))
            double numerator = recentAverageLength - lZero;
            double denominator = lengthTarget - lZero;
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
