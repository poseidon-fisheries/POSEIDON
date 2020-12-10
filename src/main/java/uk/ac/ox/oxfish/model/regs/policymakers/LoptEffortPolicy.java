package uk.ac.ox.oxfish.model.regs.policymakers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.util.DoubleSummaryStatistics;

/**
 * here we are translating EtargetLopt policy from DLMtool
 */
public class LoptEffortPolicy extends Controller {


    private final double buffer;


    private double theoreticalSuggestedEffort = 1d;


    public LoptEffortPolicy(String meanLengthColumnName,
                            double buffer,
                            double lengthTarget,
                            int averageMeanLengthOverTheseManyYears,
                            Actuator<FishState, Double> effortActuator,
                            boolean closeEntryWhenNeeded) {

        super(
                (Sensor<FishState, Double>) system -> {
                    DoubleSummaryStatistics lengthSummaryStatistic = new DoubleSummaryStatistics();
                    for (int yearBack = 0; yearBack < averageMeanLengthOverTheseManyYears; yearBack++) {
                        lengthSummaryStatistic.accept(
                                system.getYearlyDataSet().getColumn(meanLengthColumnName).
                                        getDatumXStepsAgo(yearBack)
                        );

                    }

                    return lengthSummaryStatistic.getAverage();
                },
                (Sensor<FishState, Double>) system ->
                        lengthTarget,
                closeEntryWhenNeeded ? new CloseReopenOnEffortDecorator(effortActuator):
                        effortActuator,
                365
        );
        this.buffer = buffer;
    }


    @Override
    public double computePolicy(double meanLengthColumn,
                                double lengthTarget,
                                FishState model, double oldPolicy) {


        double ratio =  meanLengthColumn / lengthTarget;
        double effort = theoreticalSuggestedEffort *(1-buffer) * (0.5 + (1d-0.5) * ratio);

        theoreticalSuggestedEffort = effort;
        return Math.min(1d,effort);

    }

    public double getBuffer() {
        return buffer;
    }

    public double getTheoreticalSuggestedEffort() {
        return theoreticalSuggestedEffort;
    }

    public void setTheoreticalSuggestedEffort(double theoreticalSuggestedEffort) {
        this.theoreticalSuggestedEffort = theoreticalSuggestedEffort;
    }
}
