/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.calibration;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStream;

import java.util.LinkedHashMap;
import java.util.SequencedMap;

import static io.jenetics.engine.Limits.byFixedGeneration;
import static io.jenetics.engine.Limits.bySteadyFitness;

public final class CalibrationRunner {

    private CalibrationRunner() {
    }

    public static Result minimize(final CalibrationProblem problem) {
        return minimize(problem, Options.defaults());
    }

    public static Result minimize(
        final CalibrationProblem problem,
        final Options options
    ) {
        final Engine<DoubleGene, Double> engine =
            Engine
                .builder(problem)
                .minimizing()
                .populationSize(options.populationSize())
                .survivorsSelector(new EliteSelector<>(
                    1,
                    new TournamentSelector<DoubleGene, Double>(3)
                ))
                .offspringSelector(new TournamentSelector<>(3))
                .alterers(
                    new GaussianMutator<>(options.mutationProbability()),
                    new MeanAlterer<>(options.recombinationProbability())
                )
                .build();

        EvolutionStream<DoubleGene, Double> stream = engine
            .stream()
            .limit(byFixedGeneration(options.generations()));

        if (options.steadyGenerations() > 0) {
            stream = stream.limit(bySteadyFitness(options.steadyGenerations()));
        }

        final EvolutionResult<DoubleGene, Double> result =
            stream
                .peek(evolutionResult -> report(evolutionResult, options))
                .collect(EvolutionResult.toBestEvolutionResult());

        return new Result(
            problem.codec().decode(result.bestPhenotype().genotype()),
            result.bestFitness(),
            result.totalGenerations()
        );
    }

    public record Options(
        int populationSize,
        long generations,
        double mutationProbability,
        double recombinationProbability,
        int steadyGenerations,
        int reportEveryGenerations
    ) {

        public Options(
            final int populationSize,
            final long generations,
            final double mutationProbability,
            final double recombinationProbability
        ) {
            this(populationSize, generations, mutationProbability, recombinationProbability, 0, 0);
        }

        public static Options defaults() {
            return new Options(30, 30, 0.20, 0.35, 10, 1);
        }
    }

    public record Result(
        SequencedMap<String, Double> parameters,
        double fitness,
        long generations
    ) {
        public Result {
            parameters = new LinkedHashMap<>(parameters);
        }

        @Override
        public SequencedMap<String, Double> parameters() {
            return new LinkedHashMap<>(parameters);
        }
    }

    private static void report(
        final EvolutionResult<DoubleGene, Double> result,
        final Options options
    ) {
        if (
            options.reportEveryGenerations() > 0 &&
                result.generation() % options.reportEveryGenerations() == 0
        ) {
            System.out.printf(
                "generation=%d best_fitness=%.6g%n",
                result.generation(),
                result.bestFitness()
            );
        }
    }
}
