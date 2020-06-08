package uk.ac.ox.oxfish.model.regs.policymakers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.utility.LinearRegression;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.function.Function;

/**
 * this object is supposed to calculate the TAC to observe next
 */
public class ISlope implements Sensor<FishState, Double> {

    /**
     * FROM THE DLM TOOLKIT DOC:
     * The TAC is calculated as:
     * TAC=TAC∗(1+λI)
     * where TAC∗ is 1−xx multiplied by the mean catch from the past yrsmth years for the first year
     * and catch from the previous year in projection years, λ is a gain parameter, and I is the slope
     * of log index over the past yrsmth years.
     */


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


    private final double gainParameterLambda;

    /**
     *  TAC* is this times the catchSensor
     */
    private final double precautionaryScaling;

    /**
     * how many years back to go
     */
    private final int maxTimeLag;

    public ISlope(String catchColumnName,
                  String indicatorColumnName,
                  double gainParameterLambda,
                  double precautionaryScaling,
                  int maxTimeLag) {
        this.catchColumnName = catchColumnName;
        this.indicatorColumnName = indicatorColumnName;
        this.gainParameterLambda = gainParameterLambda;
        this.precautionaryScaling = precautionaryScaling;
        this.maxTimeLag = maxTimeLag;
    }

    /**
     * this gets initialized the first time we have enough data and is in fact the policy suggested before
     */
    private double lastTacGiven = Double.NaN;


    @Override
    public Double scan(FishState system) {

        final DataColumn catchColumn = system.getYearlyDataSet().getColumn(catchColumnName);
        final DataColumn indicatorColumn = system.getYearlyDataSet().getColumn(indicatorColumnName);
        if(catchColumn.size()<1)
            return Double.NaN;
        assert indicatorColumn.size() == catchColumn.size();

        final int stepsBackToLook = Math.min(maxTimeLag, catchColumn.size());
        final double[] timeStep = new double[stepsBackToLook]; //will be useful for the regression

        //go backward and grab the last maxTimeLag observations
        //transforming them if necessary
        DoubleSummaryStatistics catches = new DoubleSummaryStatistics();
        double[] indicators = new double[stepsBackToLook];
        final Iterator<Double> catchesIterator = catchColumn.descendingIterator();
        final Iterator<Double> indicatorIterator = indicatorColumn.descendingIterator();
        for (int lag = 0; lag < stepsBackToLook;
             lag++) {
            catches.accept(
                    catchTransformer.apply(
                            catchesIterator.next()
                    )
            );

            indicators[stepsBackToLook-lag-1]=
                    Math.log(
                            indicatorTransformer.apply(
                                    indicatorIterator.next()
                            )
                    );
            timeStep[lag] = lag+1;
        }
        //we want the slope of the indicator
        LinearRegression regression = new LinearRegression(
                timeStep,indicators
        );
        double indicatorSlope = regression.slope();

        //the first time we are supposed to use precautionary adjustment on average catch
        //but later on we just adjust from the previous TAC
        double baseline = Double.isFinite(lastTacGiven) ?
                lastTacGiven :
                catches.getAverage() * precautionaryScaling;

        //TAC∗(1+λI)
        double newTAC =  baseline * (1+indicatorSlope*gainParameterLambda);
        if(Double.isFinite(newTAC))
        {
            lastTacGiven = newTAC;
            return lastTacGiven;
        }
        else
            return Double.NaN;



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

    public double getGainParameterLambda() {
        return gainParameterLambda;
    }

    public double getPrecautionaryScaling() {
        return precautionaryScaling;
    }

    public int getMaxTimeLag() {
        return maxTimeLag;
    }
}
