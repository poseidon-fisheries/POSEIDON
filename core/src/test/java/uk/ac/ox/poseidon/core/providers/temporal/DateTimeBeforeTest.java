/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.core.providers.temporal;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class DateTimeBeforeTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2025, 6, 1, 7, 0);

    @Test
    void subtractsTheAmountFromTheReference() {
        assertThat(new DateTimeBefore(() -> NOW, () -> Duration.ofHours(3)).get())
            .isEqualTo(NOW.minusHours(3));
    }

    @Test
    void readsTheReferenceAnewOnEveryCall() {
        final AtomicReference<LocalDateTime> referenceDateTime = new AtomicReference<>(NOW);
        final DateTimeBefore dateTimeBefore =
            new DateTimeBefore(referenceDateTime::get, () -> Duration.ofHours(3));
        dateTimeBefore.get();
        referenceDateTime.set(NOW.plusHours(1));
        assertThat(dateTimeBefore.get()).isEqualTo(NOW.minusHours(2));
    }
}
