package uk.ac.ox.oxfish.model.regs.policymakers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.util.DoubleSummaryStatistics;

/**
 * here we are translating EtargetLopt policy from DLMtool
 */
public class LoptEffortPolicy extends Controller {


    private static final long serialVersionUID = 8467863582546296898L;
    private final double buffer;


    private double theoreticalSuggestedEffort = 1d;

    private double maxChangePerYear = .1;


    public LoptEffortPolicy(
        final String meanLengthColumnName,
        final double buffer,
        final double lengthTarget,
        final int averageMeanLengthOverTheseManyYears,
        final Actuator<FishState, Double> effortActuator,
        final boolean closeEntryWhenNeeded
    ) {

        super(
            (Sensor<FishState, Double>) system -> {
                final DoubleSummaryStatistics lengthSummaryStatistic = new DoubleSummaryStatistics();
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
            closeEntryWhenNeeded ? new CloseReopenOnEffortDecorator(effortActuator) :
                effortActuator,
            365
        );
        this.buffer = buffer;
    }


    @Override
    public double computePolicy(
        final double meanLengthColumn,
        final double lengthTarget,
        final FishState model, final double oldPolicy
    ) {


        final double ratio = meanLengthColumn / lengthTarget;
        //TAE((1−buffer)(w+(1−w)r))
        final double effort = theoreticalSuggestedEffort * ((1 - buffer) * (0.5 + 0.5 * ratio));

        theoreticalSuggestedEffort = Math.max(
            Math.min(effort, theoreticalSuggestedEffort * (1d + maxChangePerYear)),
            theoreticalSuggestedEffort * (1d - maxChangePerYear)
        );

        return Math.min(1d, theoreticalSuggestedEffort);

    }

    public double getBuffer() {
        return buffer;
    }

    public double getTheoreticalSuggestedEffort() {
        return theoreticalSuggestedEffort;
    }

    public void setTheoreticalSuggestedEffort(final double theoreticalSuggestedEffort) {
        this.theoreticalSuggestedEffort = theoreticalSuggestedEffort;
    }

    /**
     * Getter for property 'maxChangePerYear'.
     *
     * @return Value for property 'maxChangePerYear'.
     */
    public double getMaxChangePerYear() {
        return maxChangePerYear;
    }

    /**
     * Setter for property 'maxChangePerYear'.
     *
     * @param maxChangePerYear Value to set for property 'maxChangePerYear'.
     */
    public void setMaxChangePerYear(final double maxChangePerYear) {
        this.maxChangePerYear = maxChangePerYear;
    }
}
