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

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.ImmutableLongArray;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import uk.ac.ox.poseidon.calibration.CalibrationProblem.SimulationTimes;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeoutException;
import java.util.function.ToDoubleFunction;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;

/** Runs an empty scenario for a day; the evaluator's score is the simulation's seed. */
class CalibrationProblemTest {

    private static CalibrationProblem problem(
        final ToDoubleFunction<Simulation> evaluator,
        final long... seeds
    ) {
        return new CalibrationProblem(
            new Scenario(LocalDate.of(2020, 1, 1), Map.of()),
            Period.ofDays(1),
            ImmutableMap.of(),
            ImmutableMap.of(),
            evaluator,
            ImmutableLongArray.copyOf(seeds)
        );
    }

    private static double evaluate(final CalibrationProblem problem) {
        return problem.fitness().apply(new LinkedHashMap<>());
    }

    @Test
    void theFitnessIsTheMeanScoreOverTheSeeds() {
        assertThat(evaluate(problem(Simulation::seed, 1, 2, 6))).isEqualTo(3.0);
    }

    /**
     * Each seed waits until the other has started: run one after the other, the first would wait
     * in vain. Two seeds, because the common pool may have a single worker on a small machine,
     * which with the calling thread still runs two at once.
     */
    @Test
    @Timeout(value = 30, threadMode = SEPARATE_THREAD)
    void runsTheSeedsOfAnEvaluationAtTheSameTime() {
        final CyclicBarrier bothRunning = new CyclicBarrier(2);
        final CalibrationProblem problem = problem(
            simulation -> {
                try {
                    bothRunning.await(10, SECONDS);
                } catch (final InterruptedException | BrokenBarrierException |
                               TimeoutException e) {
                    throw new IllegalStateException("The seeds did not run at the same time", e);
                }
                return simulation.seed();
            },
            1, 2
        );

        assertThat(evaluate(problem)).isEqualTo(1.5);
    }

    @Test
    void countsAndTimesTheSimulationsSinceTheLastCall() {
        final CalibrationProblem problem = problem(Simulation::seed, 1, 2, 3);
        evaluate(problem);
        evaluate(problem);

        final SimulationTimes times = problem.takeSimulationTimes();
        assertThat(times.simulations()).isEqualTo(6);
        assertThat(times.building()).isPositive();
        assertThat(times.total()).isGreaterThan(times.building());

        assertThat(problem.takeSimulationTimes())
            .isEqualTo(new SimulationTimes(0, Duration.ZERO, Duration.ZERO));
    }

    @Test
    void countsASimulationWhoseEvaluationFails() {
        final CalibrationProblem problem = problem(
            simulation -> {
                throw new IllegalStateException("failed");
            },
            1
        );
        try {
            evaluate(problem);
        } catch (final IllegalStateException e) {
            // expected
        }

        assertThat(problem.takeSimulationTimes().simulations()).isEqualTo(1);
    }
}
