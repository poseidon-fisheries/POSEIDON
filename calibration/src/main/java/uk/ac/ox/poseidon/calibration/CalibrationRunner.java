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

import io.jenetics.DoubleGene;
import io.jenetics.MeanAlterer;
import io.jenetics.Mutator;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;

import java.util.SequencedMap;

import static io.jenetics.engine.Limits.byFixedGeneration;

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
        final EvolutionResult<DoubleGene, Double> result =
            Engine
                .builder(problem)
                .minimizing()
                .populationSize(options.populationSize())
                .alterers(
                    new Mutator<>(options.mutationProbability()),
                    new MeanAlterer<>(options.recombinationProbability())
                )
                .build()
                .stream()
                .limit(byFixedGeneration(options.generations()))
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
        double recombinationProbability
    ) {

        public static Options defaults() {
            return new Options(20, 50, 0.15, 0.35);
        }
    }

    public record Result(
        SequencedMap<String, Double> parameters,
        double fitness,
        long generations
    ) {
    }
}
