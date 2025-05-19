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
import uk.ac.ox.oxfish.model.regs.policymakers.LTargetEffortPolicy;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.function.Function;

public class ITarget implements Sensor<FishState, Double> {
    private static final long serialVersionUID = -1299564909312928986L;

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
    private final String indicatorColumnName;
    /**
     * TAC* is (1-precautionaryScaling)C
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
     * pipe observations through these, useful for rescaling
     */
    private Function<Double, Double> catchTransformer = catches -> catches;
    /**
     * pipe observations through these, useful for rescaling: THIS HAPPENS BEFORE LOGGINg IT
     */
    private Function<Double, Double> indicatorTransformer = indicator -> indicator;
    /**
     * this is TAC* and is unchanged through the years
     */
    private double lastPolicy = Double.NaN;


    public ITarget(
        final String catchColumnName, final String indicatorColumnName,
        final double precautionaryScaling, final double indicatorMultiplier,
        final int timeInterval, final int yearsToLookBackToTarget
    ) {
        //the historical average does not get updated with time, so we need
        //a fixed sensor
        historicalAverageIndex =
            new UnchangingPastSensor(
                indicatorColumnName, 1.0,
                yearsToLookBackToTarget
            );
        this.catchColumnName = catchColumnName;
        this.indicatorColumnName = indicatorColumnName;
        this.precautionaryScaling = precautionaryScaling;
        this.indicatorMultiplier = indicatorMultiplier;
        this.timeInterval = timeInterval;
    }

    @Override
    public Double scan(final FishState system) {


        final DataColumn catchColumn = system.getYearlyDataSet().getColumn(catchColumnName);
        //you need to have at least timeInterval*2 observations
        if (catchColumn.size() < historicalAverageIndex.getYearsToLookBack()) //need a long enough time series!
            return Double.NaN;

        final double percentageChangeDueToIndicator = getPercentageChangeToTACDueToIndicator(system);

        final int stepsBackToLook = Math.min(timeInterval, catchColumn.size());

        //if this is the first time we do policy, look at average catches to form a TAC baseline
        if (!Double.isFinite(lastPolicy)) {
            final DoubleSummaryStatistics catchesThisInterval = new DoubleSummaryStatistics();
            final Iterator<Double> catchesIterator = catchColumn.descendingIterator();


            for (int lag = 0; lag < stepsBackToLook;
                 lag++) {


                catchesThisInterval.accept(
                    catchTransformer.apply(
                        catchesIterator.next()
                    ));

            }


            final double catches = catchesThisInterval.getAverage();


            lastPolicy = catches * (1 - precautionaryScaling);
            //          return lastPolicy;
        }

        return lastPolicy * percentageChangeDueToIndicator;
    }

    public Double getPercentageChangeToTACDueToIndicator(final FishState system) {
        final DataColumn indicatorColumn = system.getYearlyDataSet().getColumn(indicatorColumnName);
        final int stepsBackToLook = Math.min(timeInterval, indicatorColumn.size());
        final DoubleSummaryStatistics indicatorThisInterval = new DoubleSummaryStatistics();
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
