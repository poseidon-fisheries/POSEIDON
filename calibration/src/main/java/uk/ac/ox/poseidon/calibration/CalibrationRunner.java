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

import static io.jenetics.engine.Limits.byFixedGeneration;
import static io.jenetics.engine.Limits.bySteadyFitness;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStream;
import io.jenetics.util.BaseSeq;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.SequencedMap;
import java.util.concurrent.ForkJoinTask;
import lombok.AllArgsConstructor;
import lombok.Value;
import uk.ac.ox.poseidon.calibration.CalibrationProblem.SimulationTimes;

/**
 * Runs a {@link CalibrationProblem} through a jenetics genetic-algorithm
 * {@link Engine} (Gaussian mutation, mean-value recombination, single-elite
 * tournament selection) and returns the best solution found. Each individual is
 * evaluated in its own task on the common fork-join pool: by default jenetics
 * runs the individuals of a generation in batches of up to five, one after
 * another within a batch, which suits cheap fitness functions but not
 * simulations.
 */
public final class CalibrationRunner {

  private CalibrationRunner() {}

  /**
   * Minimizes {@code problem} using {@link Options#defaults()}.
   *
   * @return the best solution found
   */
  public static Result minimize(final CalibrationProblem problem) {
    return minimize(problem, Options.defaults());
  }

  /**
   * @param problem the calibration problem to minimize
   * @param options the genetic-algorithm parameters to run it with
   * @return the best solution found
   */
  public static Result minimize(
    final CalibrationProblem problem,
    final Options options
  ) {
    final Engine<DoubleGene, Double> engine = Engine.builder(problem)
      .minimizing()
      .populationSize(options.getPopulationSize())
      .survivorsSelector(
        new EliteSelector<>(1, new TournamentSelector<DoubleGene, Double>(3))
      )
      .offspringSelector(new TournamentSelector<>(3))
      .fitnessExecutor(CalibrationRunner::evaluateSeparately)
      .alterers(
        new GaussianMutator<>(
          options.getMutationProbability(),
          new GaussianMutator.Shape(
            options.getMutatorShapeShift(),
            options.getMutatorShapeSigma()
          )
        ),
        new MeanAlterer<>(options.getRecombinationProbability())
      )
      .build();

    final Reporter reporter = new Reporter(
      problem,
      options.getReportEveryGenerations(),
      Runtime.getRuntime().availableProcessors()
    );
    EvolutionStream<DoubleGene, Double> stream = engine
      .stream()
      .limit(byFixedGeneration(options.getGenerations()));

    if (options.getSteadyGenerations() > 0) {
      stream = stream.limit(bySteadyFitness(options.getSteadyGenerations()));
    }

    final EvolutionResult<DoubleGene, Double> result = stream
      .peek(reporter::report)
      .collect(EvolutionResult.toBestEvolutionResult());

    return new Result(
      problem.codec().decode(result.bestPhenotype().genotype()),
      result.bestFitness(),
      result.totalGenerations()
    );
  }

  /**
   * The genetic-algorithm parameters
   * {@link #minimize(CalibrationProblem, Options)} runs with.
   */
  @Value
  @AllArgsConstructor
  public static final class Options {

    /** Number of individuals per generation. */
    int populationSize;

    /** Hard cap on the number of generations to run. */
    long generations;

    /** Per-allele probability of Gaussian mutation. */
    double mutationProbability;

    /** Probability of mean-value recombination between parents. */
    double recombinationProbability;

    /**
     * If greater than {@code 0}, stop early once the best fitness hasn't
     * improved for this many consecutive generations; {@code 0} disables early
     * stopping.
     */
    int steadyGenerations;

    /**
     * If greater than {@code 0}, print a progress line every this many
     * generations: the best fitness and, since the previous line, the wall
     * time, the simulations run, their time and the share of the processors
     * they used; {@code 0} disables reporting.
     */
    int reportEveryGenerations;

    /** Shift parameter of the Gaussian mutator's shape. */
    double mutatorShapeShift;

    /** Sigma (spread) parameter of the Gaussian mutator's shape. */
    double mutatorShapeSigma;

    /**
     * Shorthand for the full constructor with early stopping, reporting and
     * mutator shaping disabled/left at their neutral defaults.
     */
    public Options(
      final int populationSize,
      final long generations,
      final double mutationProbability,
      final double recombinationProbability
    ) {
      this(
        populationSize,
        generations,
        mutationProbability,
        recombinationProbability,
        0,
        0,
        0,
        1
      );
    }

    /**
     * Shorthand for the full constructor with mutator shaping left at its
     * neutral default.
     */
    public Options(
      final int populationSize,
      final long generations,
      final double mutationProbability,
      final double recombinationProbability,
      final int steadyGenerations,
      final int reportEveryGenerations
    ) {
      this(
        populationSize,
        generations,
        mutationProbability,
        recombinationProbability,
        steadyGenerations,
        reportEveryGenerations,
        0,
        1
      );
    }

    /**
     * @return a population of 30 run for up to 30 generations (10 generations
     *         of steady fitness, whichever comes first), with a 20% mutation
     *         probability and 35% recombination probability, reporting progress
     *         every generation
     */
    public static Options defaults() {
      return new Options(30, 30, 0.20, 0.35, 10, 1, 0, 1);
    }
  }

  /**
   * The outcome of a {@link #minimize(CalibrationProblem, Options)} run.
   *
   */
  @Value
  public static final class Result {

    /**
     * The best solution found, as a map of property name to value; defensively
     * copied on construction and on every {@link #getParameters()} call.
     */
    SequencedMap<String, Double> parameters;

    /** The best solution's fitness score. */
    double fitness;

    /** The number of generations actually run. */
    long generations;

    /**
     * Copies {@code parameters}, so that later changes to the caller's map do
     * not reach the result.
     */
    public Result(
      final SequencedMap<String, Double> parameters,
      final double fitness,
      final long generations
    ) {
      this.parameters = new LinkedHashMap<>(parameters);
      this.fitness = fitness;
      this.generations = generations;
    }

    /** @return a copy of the best solution found */
    public SequencedMap<String, Double> getParameters() {
      return new LinkedHashMap<>(parameters);
    }
  }

  /**
   * Runs each of {@code evaluations} in its own fork-join task, and waits for
   * them all.
   */
  static void evaluateSeparately(
    final BaseSeq<? extends Runnable> evaluations
  ) {
    ForkJoinTask.invokeAll(
      evaluations.stream().map(ForkJoinTask::adapt).toList()
    );
  }

  /**
   * @return a progress line: {@code machine_use} is the simulations' total time
   *         over the wall time multiplied by the number of processors, so 1
   *         means every processor ran a simulation all along
   */
  static String formatReport(
    final long generation,
    final double bestFitness,
    final Duration wallTime,
    final SimulationTimes times,
    final int processors
  ) {
    final double machineUse = wallTime.isZero()
      ? 0
      : (double) times.total().toNanos() / wallTime.toNanos() / processors;
    return String.format(
      Locale.ROOT,
      "generation=%d best_fitness=%.6g wall_seconds=%.1f simulations=%d " +
        "simulation_seconds=%.1f building_seconds=%.1f processors=%d machine_use=%.2f",
      generation,
      bestFitness,
      wallTime.toNanos() / 1e9,
      times.simulations(),
      times.total().toNanos() / 1e9,
      times.building().toNanos() / 1e9,
      processors,
      machineUse
    );
  }

  /** Prints {@link #formatReport} lines, measuring the time between them. */
  private static final class Reporter {

    private final CalibrationProblem problem;
    private final int everyGenerations;
    private final int processors;
    private long previousNanos = System.nanoTime();

    private Reporter(
      final CalibrationProblem problem,
      final int everyGenerations,
      final int processors
    ) {
      this.problem = problem;
      this.everyGenerations = everyGenerations;
      this.processors = processors;
    }

    private void report(final EvolutionResult<DoubleGene, Double> result) {
      if (everyGenerations > 0 && result.generation() % everyGenerations == 0) {
        final long now = System.nanoTime();
        System.out.println(
          formatReport(
            result.generation(),
            result.bestFitness(),
            Duration.ofNanos(now - previousNanos),
            problem.takeSimulationTimes(),
            processors
          )
        );
        previousNanos = now;
      }
    }
  }
}
