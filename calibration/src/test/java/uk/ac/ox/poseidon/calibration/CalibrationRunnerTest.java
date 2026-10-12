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

import io.jenetics.util.ISeq;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import uk.ac.ox.poseidon.calibration.CalibrationProblem.SimulationTimes;

import java.time.Duration;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;
import static uk.ac.ox.poseidon.calibration.CalibrationRunner.evaluateSeparately;
import static uk.ac.ox.poseidon.calibration.CalibrationRunner.formatReport;

class CalibrationRunnerTest {

    @Test
    void reportsTheShareOfTheProcessorsTheSimulationsUsed() {
        // 6 simulations of 100 s each over 200 s on 4 processors: 600 / (200 × 4)
        assertThat(formatReport(
            3,
            1234.5678,
            Duration.ofSeconds(200),
            new SimulationTimes(6, Duration.ofSeconds(600), Duration.ofSeconds(12)),
            4
        )).isEqualTo(
            "generation=3 best_fitness=1234.57 wall_seconds=200.0 simulations=6 " +
                "simulation_seconds=600.0 building_seconds=12.0 processors=4 machine_use=0.75"
        );
    }

    @Test
    void reportsNoUseWhenNoTimePassed() {
        assertThat(formatReport(
            1,
            0.0,
            Duration.ZERO,
            new SimulationTimes(0, Duration.ZERO, Duration.ZERO),
            4
        )).endsWith("machine_use=0.00");
    }

    /**
     * Jenetics would run a batch this small in one task, one evaluation after another; each
     * evaluation here waits until another has started, so that would wait in vain.
     */
    @Test
    @Timeout(value = 30, threadMode = SEPARATE_THREAD)
    void evaluatesEachIndividualAtTheSameTimeAsTheOthers() {
        final CyclicBarrier twoRunning = new CyclicBarrier(2);
        final AtomicInteger evaluated = new AtomicInteger();
        final Runnable evaluation = () -> {
            try {
                twoRunning.await(10, SECONDS);
            } catch (final InterruptedException | BrokenBarrierException | TimeoutException e) {
                throw new IllegalStateException("The evaluations did not run at the same time", e);
            }
            evaluated.incrementAndGet();
        };

        evaluateSeparately(ISeq.of(evaluation, evaluation, evaluation, evaluation));

        assertThat(evaluated).hasValue(4);
    }
}
