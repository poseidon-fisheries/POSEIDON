package uk.ac.ox.oxfish.model.regs.policymakers.sensors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.regs.policymakers.LTargetEffortPolicy;
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

    private final UnchangingPastSensor historicalAverageIndex;

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
    private double lastPolicy = Double.NaN;



    public ITarget(String catchColumnName, String indicatorColumnName,
                   double precautionaryScaling, double indicatorMultiplier,
                   int timeInterval, int yearsToLookBackToTarget) {
        //the historical average does not get updated with time, so we need
        //a fixed sensor
        historicalAverageIndex =
                new UnchangingPastSensor(
                        indicatorColumnName,1.0,
                        yearsToLookBackToTarget
                );
        this.catchColumnName = catchColumnName;
        this.indicatorColumnName = indicatorColumnName;
        this.precautionaryScaling = precautionaryScaling;
        this.indicatorMultiplier = indicatorMultiplier;
        this.timeInterval = timeInterval;
    }


    public Double getPercentageChangeToTACDueToIndicator(FishState system){
        final DataColumn indicatorColumn = system.getYearlyDataSet().getColumn(indicatorColumnName);
        final int stepsBackToLook = Math.min(timeInterval, indicatorColumn.size());
        DoubleSummaryStatistics indicatorThisInterval = new DoubleSummaryStatistics();
        final Iterator<Double> indicatorIterator = indicatorColumn.descendingIterator();
        for (int lag = 0; lag < stepsBackToLook;
             lag++) {
            final Double observedIndicator = indicatorTransformer.apply(
                    indicatorIterator.next());
            indicatorThisInterval.accept(observedIndicator);
        }

        return LTargetEffortPolicy.computePolicyMultiplier(
                indicatorThisInterval.getAverage(),
                historicalAverageIndex.scan(system),
                indicatorMultiplier,
                0.8
        );
    }

    @Override
    public Double scan(FishState system) {


        final DataColumn catchColumn = system.getYearlyDataSet().getColumn(catchColumnName);
        //you need to have at least timeInterval*2 observations
        if(catchColumn.size()< historicalAverageIndex.getYearsToLookBack()) //need a long enough time series!
            return Double.NaN;

        double percentageChangeDueToIndicator = getPercentageChangeToTACDueToIndicator(system);

        final int stepsBackToLook = Math.min(timeInterval, catchColumn.size());

        //if this is the first time we do policy, look at average catches to form a TAC baseline
        if(!Double.isFinite(lastPolicy)) {
            DoubleSummaryStatistics catchesThisInterval = new DoubleSummaryStatistics();
            final Iterator<Double> catchesIterator = catchColumn.descendingIterator();


            for (int lag = 0; lag < stepsBackToLook;
                 lag++) {


                catchesThisInterval.accept(
                        catchTransformer.apply(
                                catchesIterator.next()
                        ));

            }


            double catches = catchesThisInterval.getAverage();


            lastPolicy = catches * (1 - precautionaryScaling);
            return lastPolicy;
        }

        return lastPolicy * percentageChangeDueToIndicator;
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
