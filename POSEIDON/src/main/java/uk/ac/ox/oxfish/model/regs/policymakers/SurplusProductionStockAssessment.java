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

package uk.ac.ox.oxfish.model.regs.policymakers;

import com.google.common.base.Preconditions;
import eva2.OptimizerFactory;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.strategies.NelderMeadSimplex;
import eva2.problems.SimpleProblemWrapper;
import eva2.problems.simple.SimpleProblemDouble;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import javax.annotation.Nullable;
import java.util.function.Function;

import static uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter.computeNumericValueFromEVABounds;

public class SurplusProductionStockAssessment implements Sensor<FishState, SurplusProductionResult> {


    private static final long serialVersionUID = -3953252428762078327L;
    private final double[] carryingCapacityBounds;
    private final double[] logisticGrowthBounds;
    private final double[] catchabilityBounds;

    private final String indicatorColumnName;
    private final String catchColumnName;
    /**
     * pipe observations through these, useful for rescaling: THIS HAPPENS BEFORE LOGGINg IT
     */
    private Function<Double, Double> indicatorTransformer = indicator -> indicator;
    /**
     * pipe observations through these, useful for rescaling
     */
    private Function<Double, Double> catchTransformer = catches -> catches;


    public SurplusProductionStockAssessment(
        final double[] carryingCapacityBounds,
        final double[] logisticGrowthBounds,
        final double[] catchabilityBounds,
        final String indicatorColumnName,
        final String catchColumnName
    ) {
        this.carryingCapacityBounds = carryingCapacityBounds;
        this.logisticGrowthBounds = logisticGrowthBounds;
        this.catchabilityBounds = catchabilityBounds;
        this.indicatorColumnName = indicatorColumnName;
        this.catchColumnName = catchColumnName;
    }

    @Override
    public SurplusProductionResult scan(final FishState system) {


        final DataColumn catchColumn = system.getYearlyDataSet().getColumn(catchColumnName);
        final DataColumn indicatorColumn = system.getYearlyDataSet().getColumn(indicatorColumnName);
        //you need to have at least timeInterval*2 observations
        assert indicatorColumn.size() == catchColumn.size();


        //collect landings and indicators
        final double[] landings = catchColumn.stream().mapToDouble(d ->

            catchTransformer.apply(d)).toArray();
        final double[] cpues = indicatorColumn.stream().mapToDouble(d ->
            indicatorTransformer.apply(d)).toArray();

        return assess(
            landings,
            cpues,
            carryingCapacityBounds,
            logisticGrowthBounds,
            catchabilityBounds
        );

    }

    @Nullable
    public static SurplusProductionResult assess(
        final double[] observedLandings,
        final double[] observedCPUE,
        final double[] carryingCapacityBounds,
        final double[] logisticGrowthBounds,
        final double[] catchabilityBounds
    ) {

        final SimpleProblemWrapper problem = new SimpleProblemWrapper();
        problem.setSimpleProblem(new MatchCPUEProblem(
            observedLandings,
            observedCPUE,
            carryingCapacityBounds,
            logisticGrowthBounds,
            catchabilityBounds

        ));
        problem.setParallelThreads(1);
        final OptimizationParameters params = OptimizerFactory.makeParams(
            NelderMeadSimplex.createNelderMeadSimplex(

                problem
                , null),
            50, problem

        );
        params.setTerminator(new EvaluationTerminator(5000));
        final double[] bestMultiplier = OptimizerFactory.optimizeToDouble(
            params
        );

        //run it with optimal parameters
        final double carryingCapacity =
            computeNumericValueFromEVABounds(
                bestMultiplier[0],
                carryingCapacityBounds[0],
                carryingCapacityBounds[1],
                true
            );
        final double logisticGrowth =
            computeNumericValueFromEVABounds(bestMultiplier[1], logisticGrowthBounds[0], logisticGrowthBounds[1], true);
        final double catchability =
            computeNumericValueFromEVABounds(bestMultiplier[2], catchabilityBounds[0], catchabilityBounds[1], true);


        final SurplusProductionResult finalSimulation = simulateSchaefer(
            carryingCapacity,
            logisticGrowth,
            catchability,
            observedLandings
        );

//        double[] simulatedCPUE = finalSimulation.getCpue();
//        double sumDistance = 0;
//        for (int year = 0; year < simulatedCPUE.length; year++) {
//
//            if(simulatedCPUE[year]<0) //negative CPUE is unacceptable: we are somewhere shit
//                return null;
//            sumDistance+= Math.pow(
//                    simulatedCPUE[year]-observedCPUE[year]
//                    ,2);
//
//        }
//        System.out.println("SP error: " + sumDistance);

        return finalSimulation;
    }

    public static SurplusProductionResult simulateSchaefer(
        final double carryingCapacity,
        final double logisticGrowth,
        final double catchability,
        final double[] observedLandings
    ) {

        Preconditions.checkArgument(observedLandings.length > 0);

        //R code:
//        biomass<- rep(NA,length(landing_time_series))
//        catches<- rep(NA,length(landing_time_series))
//        cpues<-rep(NA,length(landing_time_series))
//        for(i in 1:length(biomass))
//        {
//
//            prev_biomass<- ifelse(i==1,k,biomass[i-1])
//            catches[i] = landing_time_series[i] #q * effort_time_series[i] * prev_biomass
//            biomass[i] = prev_biomass + r * prev_biomass * (1 - prev_biomass/k) - catches[i]
//            cpues[i] = q * (prev_biomass)
//
//        }
//        return(
//                list(
//                        cpue = cpues,
//                        catches = catches,
//                        depletion = biomass/k
//                )
//        )

        //these represent "end of the year" numbers
        final double[] biomass = new double[observedLandings.length];
        final double[] depletion = new double[observedLandings.length];
        final double[] cpues = new double[observedLandings.length];

        for (int i = 0; i < biomass.length; i++) {
            final double previousBiomass = i == 0 ? carryingCapacity : biomass[i - 1];
            biomass[i] = previousBiomass +
                logisticGrowth * previousBiomass *
                    (1 - previousBiomass / carryingCapacity) -
                observedLandings[i];
            cpues[i] = catchability * previousBiomass;
            depletion[i] = biomass[i] / carryingCapacity;
        }

        return new SurplusProductionResult(
            carryingCapacity,
            logisticGrowth,
            catchability,
            depletion,
            cpues,
            observedLandings,
            biomass

        );


    }


// quick nelder mead example
//    SimpleProblemWrapper problem = new SimpleProblemWrapper();
//        problem.setSimpleProblem(new FindCorrectWeightProblem(currentCatchAtLength));
//        problem.setDefaultRange(3);
//        problem.setParallelThreads(1);
//    OptimizationParameters params = OptimizerFactory.makeParams(
//            NelderMeadSimplex.createNelderMeadSimplex(
//
//                    problem
//                    , null),
//            15,problem
//
//    );
//        params.setTerminator(new EvaluationTerminator(500));
//    double[] bestMultiplier = OptimizerFactory.optimizeToDouble(
//            params
//    );

    public Function<Double, Double> getIndicatorTransformer() {
        return indicatorTransformer;
    }

    public void setIndicatorTransformer(final Function<Double, Double> indicatorTransformer) {
        this.indicatorTransformer = indicatorTransformer;
    }

    public Function<Double, Double> getCatchTransformer() {
        return catchTransformer;
    }

    public void setCatchTransformer(final Function<Double, Double> catchTransformer) {
        this.catchTransformer = catchTransformer;
    }

    private static class MatchCPUEProblem extends SimpleProblemDouble {
        private static final long serialVersionUID = -7071847783180806663L;
        private final double[] observedLandings;

        private final double[] observedCPUE;

        private final double[] carryingCapacityBounds;

        private final double[] logisticGrowthBounds;

        private final double[] catchabilityBounds;


        public MatchCPUEProblem(
            final double[] observedLandings,
            final double[] observedCPUE,
            final double[] carryingCapacityBounds,
            final double[] logisticGrowthBounds,
            final double[] catchabilityBounds
        ) {
            this.observedLandings = observedLandings;
            this.observedCPUE = observedCPUE;
            this.carryingCapacityBounds = carryingCapacityBounds;
            this.logisticGrowthBounds = logisticGrowthBounds;
            this.catchabilityBounds = catchabilityBounds;
        }

        @Override
        public double[] evaluate(final double[] x) {
            final double carryingCapacity =
                computeNumericValueFromEVABounds(x[0], carryingCapacityBounds[0], carryingCapacityBounds[1], true);
            final double logisticGrowth =
                computeNumericValueFromEVABounds(x[1], logisticGrowthBounds[0], logisticGrowthBounds[1], true);
            final double catchability =
                computeNumericValueFromEVABounds(x[2], catchabilityBounds[0], catchabilityBounds[1], true);

            final SurplusProductionResult result = SurplusProductionStockAssessment.simulateSchaefer(
                carryingCapacity,
                logisticGrowth,
                catchability,
                observedLandings
            );

            final double[] simulatedCPUE = result.getCpue();
            double sumDistance = 0;
            for (int year = 0; year < simulatedCPUE.length; year++) {
                if (simulatedCPUE[year] < 0) //negative CPUE is unacceptable
                    return new double[]{10000000000d};
                sumDistance += Math.pow(
                    simulatedCPUE[year] - observedCPUE[year]
                    , 2);

            }


            return new double[]{sumDistance};

        }

        @Override
        public int getProblemDimension() {
            return 3;
        }
    }
}
