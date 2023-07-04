package uk.ac.ox.oxfish.model.regs.policymakers.sensors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.function.Function;

/**
 * mouthful for something relatively simple:
 * the sensor looks at a column in the past, takes the average for however many steps asked
 * maybe multiplies it by a value, and then when asked to sense again it just keeps returning
 * the same number over and over again!
 */
public class UnchangingPastSensor implements
    Sensor<FishState, Double> {


    private static final long serialVersionUID = -6222385701893032674L;
    private final String indicatorColumnName;
    /**
     * do we need to multiply the average by something?
     */
    private final double indicatorMultiplier;
    /**
     * how many years back to go
     */
    private final int yearsToLookBack;
    /**
     * pipe observations through these, useful for rescaling:
     */
    private Function<Double, Double> indicatorTransformer = indicator -> indicator;
    private double targetSet = Double.NaN;

    public UnchangingPastSensor(final String indicatorColumnName, final double indicatorMultiplier, final int yearsToLookBack) {
        this.indicatorColumnName = indicatorColumnName;
        this.indicatorMultiplier = indicatorMultiplier;
        this.yearsToLookBack = yearsToLookBack;
    }


    public UnchangingPastSensor(final String indicatorColumnName, final int yearsToLookBack) {
        this.indicatorColumnName = indicatorColumnName;
        this.indicatorMultiplier = 1.0;
        this.yearsToLookBack = yearsToLookBack;
    }

    @Override
    public Double scan(final FishState system) {

        //if you have already set the target, go no further
        if (Double.isFinite(targetSet))
            return targetSet;

        final DataColumn indicatorColumn = system.getYearlyDataSet().getColumn(indicatorColumnName);
        //you need to have at least tyearsToLookBack observations
        if (indicatorColumn.size() < yearsToLookBack) //need a long enough time series!
            return Double.NaN;

        final Iterator<Double> indicatorIterator = indicatorColumn.descendingIterator();
        final DoubleSummaryStatistics indicators = new DoubleSummaryStatistics();


        for (int lag = 0; lag < yearsToLookBack;
             lag++) {

            //transforming them if necessary
            final Double observedIndicator = indicatorTransformer.apply(
                indicatorIterator.next());
            indicators.accept(observedIndicator);
        }

        // get the target

        final double indicatorAve = indicators.getAverage();
        targetSet = indicatorAve * indicatorMultiplier;
        return targetSet;


    }


    public String getIndicatorColumnName() {
        return indicatorColumnName;
    }

    public Function<Double, Double> getIndicatorTransformer() {
        return indicatorTransformer;
    }

    public void setIndicatorTransformer(final Function<Double, Double> indicatorTransformer) {
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

    public void setTargetSet(final double targetSet) {
        this.targetSet = targetSet;
    }
}
