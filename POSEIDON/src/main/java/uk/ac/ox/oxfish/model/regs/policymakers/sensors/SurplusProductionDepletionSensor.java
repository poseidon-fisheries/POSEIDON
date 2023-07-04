package uk.ac.ox.oxfish.model.regs.policymakers.sensors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.policymakers.SurplusProductionResult;
import uk.ac.ox.oxfish.model.regs.policymakers.SurplusProductionStockAssessment;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.util.function.Function;

/**
 * returns the current depletion level according to a surplus-production model assessment
 */
public class SurplusProductionDepletionSensor implements Sensor<FishState, Double> {

    private static final long serialVersionUID = 3818329073309046080L;
    private final SurplusProductionStockAssessment assessment;

    public SurplusProductionDepletionSensor(
        final double[] carryingCapacityBounds, final double[] logisticGrowthBounds,
        final double[] catchabilityBounds, final String indicatorColumnName,
        final String catchColumnName
    ) {

        assessment = new SurplusProductionStockAssessment(
            carryingCapacityBounds,
            logisticGrowthBounds,
            catchabilityBounds,
            indicatorColumnName,
            catchColumnName
        );
    }


    @Override
    public Double scan(final FishState system) {

        final SurplusProductionResult assessmentResult = assessment.scan(system);
        if (assessmentResult == null)
            return Double.NaN;
        else
            return assessmentResult.getDepletion()[assessmentResult.getDepletion().length];


    }

    public Function<Double, Double> getCatchTransformer() {
        return assessment.getCatchTransformer();
    }

    public void setCatchTransformer(final Function<Double, Double> catchTransformer) {
        assessment.setCatchTransformer(catchTransformer);
    }

    public Function<Double, Double> getIndicatorTransformer() {
        return assessment.getIndicatorTransformer();
    }

    public void setIndicatorTransformer(final Function<Double, Double> indicatorTransformer) {
        assessment.setIndicatorTransformer(indicatorTransformer);
    }


}
