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

import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;

@RequiredArgsConstructor
public class QuickRunner {
    private final Scenario scenario;

    public void runFor(final TemporalAmount temporalAmount) {
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
}
