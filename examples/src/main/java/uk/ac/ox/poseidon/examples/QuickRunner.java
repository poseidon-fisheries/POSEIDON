/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.examples;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.PathConverter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.io.ScenarioLoader;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.TemporalAmount;

@NoArgsConstructor
@AllArgsConstructor
public class QuickRunner implements Runnable {

    @Parameter(
        names = {"-s", "--scenario"},
        description = "Path to the scenario file in YAML format",
        converter = PathConverter.class
    )
    private Path scenarioPath;

    @Parameter(
        names = {"-p", "--period"},
        description =
            "Period to run the simulation for in ISO-8601 format (e.g., P2Y for two years). " +
                "See https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/" +
                "Period.html#parse(java.lang.CharSequence).",
        converter = PeriodConverter.class
    )
    private Period period;

    public static void main(final String[] args) {
        // TODO
    }

    private void run(
        final Scenario scenario,
        final TemporalAmount temporalAmount
    ) {
        final Simulation simulation = scenario.newSimulation();
        simulation.start();
        final LocalDateTime end =
            scenario.getStartingDateTime().get(simulation).plus(temporalAmount);
        while (
            simulation
                .getTemporalSchedule()
                .getDateTime()
                .isBefore(end)
        ) {
            simulation.schedule.step(simulation);
        }
        simulation.finish();
    }

    @Override
    public void run() {
        run(
            new ScenarioLoader().load(scenarioPath.toFile()),
            period
        );
    }

    private static class PeriodConverter implements IStringConverter<Period> {
        @Override
        public Period convert(final String value) {
            return Period.parse(value);
        }
    }
}
