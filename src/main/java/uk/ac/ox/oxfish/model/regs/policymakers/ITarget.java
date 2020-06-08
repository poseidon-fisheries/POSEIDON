package uk.ac.ox.oxfish.model.regs.policymakers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.utility.LinearRegression;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.function.Function;

public class ITarget implements Sensor<FishState,Double> {

//    The TAC is calculated as: If Irecent≥I0:
//    TAC=0.5TAC∗[1+(Irecent−I0)/(Itarget−I0)]
//            else:
//    TAC=0.5TAC∗[IrecentI02]
//    where I0 is 0.8Iave (the average index over the
//    2 x yrsmth years prior to the projection period),
//    Irecent is the average index over the past yrsmth years, and
//    Itarget is Imulti times Iave, and
//    TAC∗ is:
//            (1−x)C
//    where x is argument xx and C is the average catch over
//    the last 5 years of the historical period.


    private final String catchColumnName;


    /**
     * pipe observations through these, useful for rescaling
     */
    private Function<Double,Double> catchTransformer = catches -> catches;


    private final String indicatorColumnName;


    /**
     * pipe observations through these, useful for rescaling: THIS HAPPENS BEFORE LOGGINg IT
     */
    private Function<Double,Double> indicatorTransformer = indicator -> indicator;


    /**
     *  TAC* is (1-precautionaryScaling)C
     */
    private final double precautionaryScaling;

    /**
     * Itarget is IAverage*IndicatorMultiplier
     */
    private final double indicatorMultiplier;

    /**
     * how many years back to go
     */
    private final int timeInterval;


    /**
     * this is TAC* and is unchanged through the years
     */
    private double tacStar = Double.NaN;

    public ITarget(String catchColumnName, String indicatorColumnName, double precautionaryScaling, double indicatorMultiplier, int timeInterval) {
        this.catchColumnName = catchColumnName;
        this.indicatorColumnName = indicatorColumnName;
        this.precautionaryScaling = precautionaryScaling;
        this.indicatorMultiplier = indicatorMultiplier;
        this.timeInterval = timeInterval;
    }

    @Override
    public Double scan(FishState system) {

        final DataColumn catchColumn = system.getYearlyDataSet().getColumn(catchColumnName);
        final DataColumn indicatorColumn = system.getYearlyDataSet().getColumn(indicatorColumnName);
        //you need to have at least timeInterval*2 observations
        if(catchColumn.size()<timeInterval*2) //need a long enough time series!
            return Double.NaN;
        assert indicatorColumn.size() == catchColumn.size();


        final int stepsBackToLook = Math.min(timeInterval, catchColumn.size());

        //go backward and grab the last maxTimeLag observations
        //transforming them if necessary
        DoubleSummaryStatistics catchesThisInterval = new DoubleSummaryStatistics();
        DoubleSummaryStatistics indicatorThisInterval = new DoubleSummaryStatistics();
        DoubleSummaryStatistics indicatorBothIntervals = new DoubleSummaryStatistics();
        final Iterator<Double> catchesIterator = catchColumn.descendingIterator();
        final Iterator<Double> indicatorIterator = indicatorColumn.descendingIterator();


        for (int lag = 0; lag < stepsBackToLook;
             lag++) {


            catchesThisInterval.accept(
                    catchTransformer.apply(
                            catchesIterator.next()
            ));
            final Double observedIndicator = indicatorTransformer.apply(
                    indicatorIterator.next());

            indicatorBothIntervals.accept(observedIndicator);
            indicatorThisInterval.accept(observedIndicator);
        }
        //then once more, but only for BothIntervals
        for (int lag = 0; lag < stepsBackToLook;
             lag++) {
            indicatorBothIntervals.accept(indicatorTransformer.apply(
                    indicatorIterator.next()));

        }

        double indicatorAve = indicatorBothIntervals.getAverage();
        double indicatorZero = indicatorAve * 0.8;
        double indicatorRecent = indicatorThisInterval.getAverage();
        double indicatorTarget = indicatorAve* indicatorMultiplier;

        double catches = catchesThisInterval.getAverage();

        //create baseline if it's not there!
        if(!Double.isFinite(tacStar))
             tacStar = catches * (1-precautionaryScaling);

        if(indicatorRecent<=indicatorZero) {
            //0.5 * TACstar * (Irecent/I0)^2
            return  0.5*tacStar* Math.pow(indicatorRecent/indicatorZero,2);
        }
        else
            //TAC=0.5TAC∗[1+(Irecent−I0)/(Itarget−I0)]
            return 0.5*tacStar * (1d+ (indicatorRecent-indicatorZero)/
                    (indicatorTarget-indicatorZero));
    }


    public String getCatchColumnName() {
        return catchColumnName;
    }

    public Function<Double, Double> getCatchTransformer() {
        return catchTransformer;
    }

    public void setCatchTransformer(Function<Double, Double> catchTransformer) {
        this.catchTransformer = catchTransformer;
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

    public double getPrecautionaryScaling() {
        return precautionaryScaling;
    }

    public double getIndicatorMultiplier() {
        return indicatorMultiplier;
    }

    public int getTimeInterval() {
        return timeInterval;
    }
}
