/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.regs.policymakers.sensors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.utility.LinearRegression;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.function.Function;

/**
 * this object is supposed to calculate the TAC to observe next
 */
public class ISlope implements Sensor<FishState, Double> {

    private static final long serialVersionUID = 153083663790670385L;
    /**
     * FROM THE DLM TOOLKIT DOC:
     * The TAC is calculated as:
     * TAC=TAC∗(1+λI)
     * where TAC∗ is 1−xx multiplied by the mean catch from the past yrsmth years for the first year
     * and catch from the previous year in projection years, λ is a gain parameter, and I is the slope
     * of log index over the past yrsmth years.
     */


    private final String catchColumnName;
    private final String indicatorColumnName;
    private final double gainParameterLambda;
    /**
     * TAC* is this times the catchSensor
     */
    private final double precautionaryScaling;
    /**
     * how many years back to go
     */
    private final int maxTimeLag;
    /**
     * pipe observations through these, useful for rescaling
     */
    private Function<Double, Double> catchTransformer = catches -> catches;
    /**
     * pipe observations through these, useful for rescaling: THIS HAPPENS BEFORE LOGGINg IT
     */
    private Function<Double, Double> indicatorTransformer = indicator -> indicator;
    /**
     * this gets initialized the first time we have enough data and is in fact the policy suggested before
     */
    private double lastTacGiven = Double.NaN;

    public ISlope(
        final String catchColumnName,
        final String indicatorColumnName,
        final double gainParameterLambda,
        final double precautionaryScaling,
        final int maxTimeLag
    ) {
        this.catchColumnName = catchColumnName;
        this.indicatorColumnName = indicatorColumnName;
        this.gainParameterLambda = gainParameterLambda;
        this.precautionaryScaling = precautionaryScaling;
        this.maxTimeLag = maxTimeLag;
    }

    @Override
    public Double scan(final FishState system) {

        final DataColumn catchColumn = system.getYearlyDataSet().getColumn(catchColumnName);
        final DataColumn indicatorColumn = system.getYearlyDataSet().getColumn(indicatorColumnName);
        if (catchColumn.size() < 1)
            return Double.NaN;
        assert indicatorColumn.size() == catchColumn.size();

        final int stepsBackToLook = Math.min(maxTimeLag, catchColumn.size());
        final double[] timeStep = new double[stepsBackToLook]; //will be useful for the regression

        //go backward and grab the last maxTimeLag observations
        //transforming them if necessary
        final DoubleSummaryStatistics catches = new DoubleSummaryStatistics();
        final double[] indicators = new double[stepsBackToLook];
        final Iterator<Double> catchesIterator = catchColumn.descendingIterator();
        final Iterator<Double> indicatorIterator = indicatorColumn.descendingIterator();
        for (int lag = 0; lag < stepsBackToLook;
             lag++) {
            catches.accept(
                catchTransformer.apply(
                    catchesIterator.next()
                )
            );

            indicators[stepsBackToLook - lag - 1] =
                Math.log(
                    indicatorTransformer.apply(
                        indicatorIterator.next()
                    )
                );
            timeStep[lag] = lag + 1;
        }
        //we want the slope of the indicator
        final LinearRegression regression = new LinearRegression(
            timeStep, indicators
        );
        final double indicatorSlope = regression.slope();

        //the first time we are supposed to use precautionary adjustment on average catch
        //but later on we just adjust from the previous TAC
        final double baseline = Double.isFinite(lastTacGiven) ?
            lastTacGiven :
            catches.getAverage() * precautionaryScaling;

        //TAC∗(1+λI)
        final double newTAC = baseline * (1 + indicatorSlope * gainParameterLambda);
        if (Double.isFinite(newTAC)) {
            lastTacGiven = newTAC;
            return lastTacGiven;
        } else
            return Double.NaN;


    }

    public String getCatchColumnName() {
        return catchColumnName;
    }

    public Function<Double, Double> getCatchTransformer() {
        return catchTransformer;
    }

    public void setCatchTransformer(final Function<Double, Double> catchTransformer) {
        this.catchTransformer = catchTransformer;
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
