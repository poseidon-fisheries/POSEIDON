package uk.ac.ox.oxfish.model.regs.policymakers.sensors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.function.Function;

/**
 * mouthful for something relatively simple:
 * takes the ITarget way of setting a target: average indicator in the past X years
 * times a multiplier.
 * Once that's set, the sensor never updates its target
 */
public class FixedTargetAsMultipleOfOriginalObservation implements
        Sensor<FishState,Double> {


    private final String indicatorColumnName;


    /**
     * pipe observations through these, useful for rescaling: THIS HAPPENS BEFORE LOGGINg IT
     */
    private Function<Double,Double> indicatorTransformer = indicator -> indicator;

    /**
     * Itarget is IAverage*IndicatorMultiplier
     */
    private final double indicatorMultiplier;

    /**
     * how many years back to go
     */
    private final int yearsToLookBack;

    private double targetSet = Double.NaN;

    public FixedTargetAsMultipleOfOriginalObservation(String indicatorColumnName, double indicatorMultiplier, int yearsToLookBack) {
        this.indicatorColumnName = indicatorColumnName;
        this.indicatorMultiplier = indicatorMultiplier;
        this.yearsToLookBack = yearsToLookBack;
    }

    @Override
    public Double scan(FishState system) {

        //if you have already set the target, go no further
        if(Double.isFinite(targetSet))
            return targetSet;

        final DataColumn indicatorColumn = system.getYearlyDataSet().getColumn(indicatorColumnName);
        //you need to have at least tyearsToLookBack observations
        if(indicatorColumn.size()<yearsToLookBack) //need a long enough time series!
            return Double.NaN;

        final Iterator<Double> indicatorIterator = indicatorColumn.descendingIterator();
        DoubleSummaryStatistics indicators = new DoubleSummaryStatistics();


        for (int lag = 0; lag < yearsToLookBack;
             lag++) {

            //transforming them if necessary
            final Double observedIndicator = indicatorTransformer.apply(
                    indicatorIterator.next());
            indicators.accept(observedIndicator);
        }

        // get the target

        double indicatorAve = indicators.getAverage();
        targetSet = indicatorAve* indicatorMultiplier;
        return targetSet;


    }


    public String getIndicatorColumnName() {
        return indicatorColumnName;
    }

    public Function<Double, Double> getIndicatorTransformer() {
        return indicatorTransformer;
    }

    public void setIndicatorTransformer(Function<Double, Double> indicatorTransformer) {
        this.indicatorTransformer = indicatorTransformer;
    }

    public double getIndicatorMultiplier() {
        return indicatorMultiplier;
    }

    public int getYearsToLookBack() {
        return yearsToLookBack;
    }

    public double getTargetSet() {
        return targetSet;
    }

    public void setTargetSet(double targetSet) {
        this.targetSet = targetSet;
    }
}
