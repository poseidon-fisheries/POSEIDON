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

import static java.util.stream.Collectors.toMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.ImmutableLongArray;
import io.jenetics.DoubleChromosome;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Problem;
import io.jenetics.util.DoubleRange;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.SequencedMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

/**
 * A jenetics {@link Problem} that calibrates some of a {@link Scenario}'s
 * values by running it forward for a fixed duration, once per seed, and
 * scoring the outcome with an {@code evaluator}. A candidate solution is a map
 * of address to value, each value encoded as an allele in {@code [0.0, 1.0]}
 * that {@link #codec()} rescales to that address's {@code parameterRanges}
 * entry; {@link #fitness()} derives a scenario with those values (see
 * {@link Scenario#with}), runs it for every seed in {@code seeds} and returns
 * the average evaluator score. The seeds of one evaluation run in parallel, on the
 * common fork-join pool (where jenetics also evaluates the population), so the
 * simulations running at once are bounded by the processors. It counts the
 * simulations it runs and how long they take; see
 * {@link #takeSimulationTimes()}.
 */
@RequiredArgsConstructor
@Accessors(fluent = true)
public class CalibrationProblem
  implements Problem<SequencedMap<String, Double>, DoubleGene, Double>
{

  /**
   * The scenario being calibrated, from which each candidate derives its own.
   */
  protected final @NonNull Scenario scenario;
  private final @NonNull TemporalAmount duration;
  private final @NonNull ImmutableMap<String, DoubleRange> parameterRanges;
  private final @NonNull ToDoubleFunction<Simulation> evaluator;
  private final @NonNull ImmutableLongArray seeds;
  private final LongAdder simulations = new LongAdder();
  private final LongAdder simulationNanos = new LongAdder();
  private final LongAdder buildingNanos = new LongAdder();

  /**
   * Encodes/decodes a genotype of {@code parameterRanges.size()} alleles in
   * {@code [0.0, 1.0]} to/from a map of address to value, by linearly
   * rescaling each allele to its corresponding entry in
   * {@code parameterRanges}. Computed lazily, once, on first access.
   */
  @Getter(lazy = true)
  private final Codec<SequencedMap<String, Double>, DoubleGene> codec =
    Codec.of(
      Genotype.of(DoubleChromosome.of(0.0, 1.0, this.parameterRanges.size())),
      genotype -> {
        final var entries = this.parameterRanges.entrySet().stream().toList();
        final var chromosome = genotype.chromosome();
        return IntStream.range(0, this.parameterRanges.size())
          .mapToObj(i -> {
            final var entry = entries.get(i);
            final DoubleRange range = entry.getValue();
            final double allele = chromosome.get(i).allele();
            return java.util.Map.entry(
              entry.getKey(),
              range.min() + allele * (range.max() - range.min())
            );
          })
          .collect(
            toMap(
              java.util.Map.Entry::getKey,
              java.util.Map.Entry::getValue,
              (_, b) -> b,
              LinkedHashMap::new
            )
          );
      }
    );

  /**
   * @return a function that derives a scenario from {@link #scenario} with
   *         the given values, runs it once per seed in {@code seeds}, and
   *         returns the average of {@code evaluator}'s score across seeds
   */
  public Function<SequencedMap<String, Double>, Double> fitness() {
    return this::evaluate;
  }

  /**
   * @return the number of simulations run, and their total time, since the
   *         previous call (or since this problem was created); meant to be read
   *         between generations, when no simulation is running
   */
  public SimulationTimes takeSimulationTimes() {
    return new SimulationTimes(
      simulations.sumThenReset(),
      Duration.ofNanos(simulationNanos.sumThenReset()),
      Duration.ofNanos(buildingNanos.sumThenReset())
    );
  }

  private double evaluate(final SequencedMap<String, Double> parameters) {
    Scenario candidate = scenario;
    for (final var parameter : parameters.entrySet()) {
      candidate = candidate.with(parameter.getKey(), parameter.getValue());
    }
    final Scenario derived = candidate;
    // Scores are collected in seed order and averaged sequentially, so the
    // result does not depend on which simulation finishes first
    final double[] scores = seeds
      .stream()
      .parallel()
      .mapToDouble(seed -> evaluate(derived, seed))
      .toArray();
    return Arrays.stream(scores).average().orElseThrow();
  }

  private double evaluate(final Scenario candidate, final long seed) {
    final long start = System.nanoTime();
    try {
      final Simulation simulation = candidate.startNewSimulation(seed);
      buildingNanos.add(System.nanoTime() - start);
      try {
        final TemporalSchedule temporalSchedule =
          simulation.getTemporalSchedule();
        temporalSchedule.stepFor(simulation, duration);
        return evaluator.applyAsDouble(simulation);
      } finally {
        simulation.finish();
      }
    } finally {
      simulations.increment();
      simulationNanos.add(System.nanoTime() - start);
    }
  }

  /**
   * Simulations run over some period, with their total time.
   *
   * @param simulations the number of simulations run
   * @param total       their total time, from building each one to finishing it
   * @param building    the part of {@code total} spent building them
   */
  public record SimulationTimes(
    long simulations,
    Duration total,
    Duration building
  ) {}
}
