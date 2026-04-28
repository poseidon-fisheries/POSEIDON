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
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.TreeMap;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.summingDouble;

public final class PeterSnapperCatchabilityCalibration {

    private static final Path LANDINGS_PATH =
        Path.of("POSEIDON", "examples", "inputs", "peter_snapper", "landings.csv");
    private static final String CATCHABILITY_PROPERTY =
        "components(gear).proportion";
    private static final double MIN_CATCHABILITY = 0.00001;
    private static final double MAX_CATCHABILITY = 0.005;

    private PeterSnapperCatchabilityCalibration() {
    }

    public static void main(final String[] args) throws IOException {
        final int populationSize = args.length > 0 ? Integer.parseInt(args[0]) : 20;
        final long generations = args.length > 1 ? Long.parseLong(args[1]) : 50;
        final List<Double> observedLandings = readLandings(LANDINGS_PATH);
        final LandingsCalibrationProblem problem =
            new LandingsCalibrationProblem(observedLandings);

        final CalibrationRunner.Result result =
            CalibrationRunner.minimize(
                problem,
                new CalibrationRunner.Options(
                    populationSize,
                    generations,
                    0.15,
                    0.35
                )
            );

        System.out.println("Best parameters: " + result.parameters());
        System.out.println("Best fitness: " + result.fitness());
        System.out.println("Generations: " + result.generations());
        printLandingsComparison(
            observedLandings,
            problem.landingsFor(result.parameters())
        );
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

        private LandingsCalibrationProblem(final List<Double> observedLandings) {
            super(
                new PeterSnapperScenario().get(),
                Period.ofYears(observedLandings.size()),
                List.of(new ParameterRange(
                    CATCHABILITY_PROPERTY,
                    MIN_CATCHABILITY,
                    MAX_CATCHABILITY
                )),
                0
            );
            this.observedLandings = observedLandings;
        }

        private List<Double> landingsFor(
            final SequencedMap<String, Double> parameters
        ) {
            final Simulation simulation =
                scenario().startNewSimulation(
                    uk.ac.ox.poseidon.core.SimulationStartOptions
                        .builder()
                        .seed(0)
                        .propertyOverrides(new java.util.LinkedHashMap<String, Object>(parameters))
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
                annualLandingsKg(simulation);
            final int firstYear =
                simulation.getTemporalSchedule().getStartingDateTime().getYear();
            return IntStream
                .range(0, observedLandings.size())
                .mapToObj(i -> annualLandings.getOrDefault(firstYear + i, 0.0))
                .toList();
        }

        private Map<Integer, Double> annualLandingsKg(final Simulation simulation) {
            return simulation
                .getComponent(uk.ac.ox.poseidon.agents.market.BiomassSaleAccumulator.class)
                .getEvents()
                .collect(
                    groupingBy(
                        sale -> sale.getDateTime().getYear(),
                        TreeMap::new,
                        mapping(
                            sale -> sale
                                .getItems()
                                .stream()
                                .mapToDouble(item -> item.getContent().asKg())
                                .sum(),
                            summingDouble(Double::doubleValue)
                        )
                    )
                );
        }
    }
}
