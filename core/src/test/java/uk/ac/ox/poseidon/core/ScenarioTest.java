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

package uk.ac.ox.poseidon.core;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.time.DateTimeFactory;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.*;

class ScenarioTest {

    @Test
    void restoresPropertyOverridesAfterStartingSimulation() {
        final DateTimeFactory startingDateTime =
            new DateTimeFactory(2000, 1, 1, 0, 0, 0);
        final Scenario scenario =
            Scenario.builder()
                .startingDateTime(startingDateTime)
                .build();

        final Simulation simulation =
            scenario.startNewSimulation(
                SimulationStartOptions
                    .builder()
                    .propertyOverrides(propertyOverrides(
                        "startingDateTime.year",
                        2001
                    ))
                    .build()
            );

        try {
            assertEquals(
                LocalDateTime.of(2001, 1, 1, 0, 0, 0),
                simulation.getTemporalSchedule().getStartingDateTime()
            );
            assertEquals(2000, startingDateTime.getYear());
        } finally {
            simulation.finish();
        }
    }

    @Test
    void restoresPropertyOverridesWhenStartingSimulationFails() {
        final DateTimeFactory startingDateTime =
            new DateTimeFactory(2000, 1, 1, 0, 0, 0);
        final Scenario scenario =
            Scenario.builder()
                .startingDateTime(startingDateTime)
                .build();

        assertThrows(
            RuntimeException.class,
            () -> scenario.startNewSimulation(
                SimulationStartOptions
                    .builder()
                    .propertyOverrides(propertyOverrides(
                        "startingDateTime.month",
                        13
                    ))
                    .build()
            )
        );
        assertEquals(1, startingDateTime.getMonth());
    }

    @Test
    void appliesPropertyOverridesInEncounterOrder() {
        final DateTimeFactory startingDateTime =
            new DateTimeFactory(2000, 1, 1, 0, 0, 0);
        final Scenario scenario =
            Scenario.builder()
                .startingDateTime(startingDateTime)
                .build();
        final SequencedMap<String, Object> propertyOverrides = new LinkedHashMap<>();
        propertyOverrides.put(
            "startingDateTime",
            new DateTimeFactory(1999, 1, 1, 0, 0, 0)
        );
        propertyOverrides.put("startingDateTime.year", 2001);

        final Simulation simulation =
            scenario.startNewSimulation(
                SimulationStartOptions
                    .builder()
                    .propertyOverrides(propertyOverrides)
                    .build()
            );

        try {
            assertEquals(
                LocalDateTime.of(2001, 1, 1, 0, 0, 0),
                simulation.getTemporalSchedule().getStartingDateTime()
            );
            assertSame(startingDateTime, scenario.getStartingDateTime());
        } finally {
            simulation.finish();
        }
    }

    private static SequencedMap<String, Object> propertyOverrides(
        final String propertyName,
        final Object value
    ) {
        final SequencedMap<String, Object> propertyOverrides = new LinkedHashMap<>();
        propertyOverrides.put(propertyName, value);
        return propertyOverrides;
    }
}
