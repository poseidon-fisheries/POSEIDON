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

package uk.ac.ox.poseidon.examples.petersnapper;

import uk.ac.ox.poseidon.calibration.CalibrationProblem;
import uk.ac.ox.poseidon.calibration.CalibrationRunner;
import uk.ac.ox.poseidon.calibration.ParameterRange;
import uk.ac.ox.poseidon.core.Simulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Period;
import java.util.*;
import java.util.stream.IntStream;

import static java.lang.Math.abs;

public final class PeterSnapperCatchabilityCalibration {

    private static final Path LANDINGS_PATH =
        Path.of("POSEIDON", "examples", "inputs", "peter_snapper", "landings.csv");
    private static final String CATCHABILITY_PROPERTY =
        "components(gear).proportion";
    private static final double MIN_CATCHABILITY = 0.00001;
    private static final double MAX_CATCHABILITY = 0.005;
    private static final double TUTORIAL_CATCHABILITY = 0.000641964;
    private static final int DEFAULT_POPULATION_SIZE = 30;
    private static final long DEFAULT_GENERATIONS = 30;
    private static final int STEADY_GENERATIONS = 10;
    private static final double MUTATION_PROBABILITY = 0.20;
    private static final double RECOMBINATION_PROBABILITY = 0.35;
    private static final long[] DEFAULT_SEEDS = {0};

    private PeterSnapperCatchabilityCalibration() {
    }

    static void main(final String[] args) throws IOException {
        final int populationSize =
            args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_POPULATION_SIZE;
        final long generations =
            args.length > 1 ? Long.parseLong(args[1]) : DEFAULT_GENERATIONS;
        final long[] seeds = parseSeeds(args);
        final List<Double> observedLandings = readLandings(LANDINGS_PATH);
        final LandingsCalibrationProblem problem =
            new LandingsCalibrationProblem(observedLandings, seeds);

        System.out.println("population_size=" + populationSize);
        System.out.println("generations=" + generations);
        System.out.println("steady_generations=" + STEADY_GENERATIONS);
        System.out.println("seeds=" + Arrays.toString(seeds));
        System.out.println("tutorial_catchability=" + TUTORIAL_CATCHABILITY);
        System.out.println("tutorial_fitness=" + problem.fitnessFor(tutorialParameters()));

        final CalibrationRunner.Result result =
            CalibrationRunner.minimize(
                problem,
                new CalibrationRunner.Options(
                    populationSize,
                    generations,
                    MUTATION_PROBABILITY,
                    RECOMBINATION_PROBABILITY,
                    STEADY_GENERATIONS,
                    1
                )
            );

        System.out.println("Best parameters: " + result.parameters());
        System.out.println("Best fitness: " + result.fitness());
        System.out.println("Generations: " + result.generations());
        System.out.printf(
            "Tutorial catchability error: %.6g%n",
            result.parameters().get(CATCHABILITY_PROPERTY) - TUTORIAL_CATCHABILITY
        );
        printLandingsComparison(
            observedLandings,
            problem.landingsFor(result.parameters())
        );
    }

    private static long[] parseSeeds(final String[] args) {
        return args.length > 2
            ? Arrays
            .stream(args)
            .skip(2)
            .mapToLong(Long::parseLong)
            .toArray()
            : DEFAULT_SEEDS;
    }

    private static SequencedMap<String, Double> tutorialParameters() {
        final SequencedMap<String, Double> parameters = new LinkedHashMap<>();
        parameters.put(CATCHABILITY_PROPERTY, TUTORIAL_CATCHABILITY);
        return parameters;
    }

    private static List<Double> readLandings(final Path path) throws IOException {
        return Files
            .lines(path)
            .skip(1)
            .map(line -> line.split(","))
            .map(columns -> Double.parseDouble(columns[1]))
            .toList();
    }

    private static void printLandingsComparison(
        final List<Double> observedLandings,
        final List<Double> simulatedLandings
    ) {
        System.out.println("year,observed_kg,simulated_kg,error_kg");
        IntStream
            .range(0, observedLandings.size())
            .forEach(i ->
                System.out.printf(
                    "%d,%.0f,%.0f,%.0f%n",
                    i + 1,
                    observedLandings.get(i),
                    simulatedLandings.get(i),
                    simulatedLandings.get(i) - observedLandings.get(i)
                )
            );
    }

    private static final class LandingsCalibrationProblem extends CalibrationProblem {

        private final List<Double> observedLandings;

        private LandingsCalibrationProblem(
            final List<Double> observedLandings,
            final long... seeds
        ) {
            super(
                new PeterSnapperScenario().get(),
                Period.ofYears(observedLandings.size()),
                List.of(new ParameterRange(
                    CATCHABILITY_PROPERTY,
                    MIN_CATCHABILITY,
                    MAX_CATCHABILITY
                )),
                seeds
            );
            this.observedLandings = observedLandings;
        }

        private double fitnessFor(
            final SequencedMap<String, Double> parameters
        ) {
            return fitness().apply(parameters);
        }

        private List<Double> landingsFor(
            final SequencedMap<String, Double> parameters
        ) {
            final Simulation simulation =
                scenario().startNewSimulation(
                    uk.ac.ox.poseidon.core.SimulationStartOptions
                        .builder()
                        .seed(seeds()[0])
                        .propertyOverrides(new LinkedHashMap<String, Object>(parameters))
                        .build()
                );
            try {
                simulation
                    .getTemporalSchedule()
                    .stepFor(simulation, Period.ofYears(observedLandings.size()));
                return simulatedLandings(simulation);
            } finally {
                simulation.finish();
            }
        }

        @Override
        protected double evaluate(final Simulation simulation) {
            final List<Double> simulatedLandings = simulatedLandings(simulation);
            return IntStream
                .range(0, observedLandings.size())
                .mapToDouble(i ->
                    abs(observedLandings.get(i) - simulatedLandings.get(i))
                )
                .sum();
        }

        private List<Double> simulatedLandings(final Simulation simulation) {
            final Map<Integer, Double> annualLandings =
                simulation
                    .getComponent(TotalLandingsPerYearAccumulator.class)
                    .get();
            final int firstYear =
                simulation.getTemporalSchedule().getStartingDateTime().getYear();
            return IntStream
                .range(0, observedLandings.size())
                .mapToObj(i -> annualLandings.getOrDefault(firstYear + i, 0.0))
                .toList();
        }

    }
}
